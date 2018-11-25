package com.pyg.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import javax.security.auth.login.CredentialException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {


        //关键字处理空格问题
        String keywords = ((String) searchMap.get("keywords")).replaceAll(" ", "");
        searchMap.put("keywords",keywords);

        Map resultMap=new HashMap();
        //1.查询商品(设置有高亮显示的总数据)
        Map itemMap =searchList(searchMap);
        resultMap.putAll(itemMap);


        //2.查询分类数据，分组查询
        List<String> categoryList = findCategoryList(searchMap);
        resultMap.put("categoryList", categoryList);
        //3.查询品牌和规格数据
        //默认没有选择分类时，使用分类列表中的第一个分类名称去查，如果选择分类了那么久按照选择的去查
        if (categoryList.size()>0){
            Map brandAndSpecList=new HashMap();
            if(!"".equals(searchMap.get("category"))){
                brandAndSpecList = findBrandAndSpecList((String) searchMap.get("category"));
            }else{
                brandAndSpecList = findBrandAndSpecList(categoryList.get(0));
            }
            resultMap.putAll(brandAndSpecList);
        }


        return resultMap;    //{rows:[],total:149,categoryList:[],brandList:[],specList:[]}
    }


    //1高亮查询
    private Map searchList(Map searchMap) {
        Map itemMap=new HashMap();

        HighlightQuery query=new SimpleHighlightQuery();
        //1.1按照关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //1.2设置高亮选项
        HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//设置高亮域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix("</em>");//设置高亮后缀
        query.setHighlightOptions(highlightOptions);//设置高亮选项

        //1.3按分类查询
        if(searchMap.get("category")!=null&&!"".equals(searchMap.get("category"))){//有条件
            FilterQuery filterQuery=new SimpleFacetQuery();
            Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);

        }
        //1.4按品牌查询
        if(searchMap.get("category")!=null&&!"".equals(searchMap.get("brand"))){
            FilterQuery filterQuery=new SimpleFacetQuery();
            Criteria filterCriteria=new Criteria("brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.5按规格查询
        Map<String,String> specMap = (Map) searchMap.get("spec");
        for (String key : specMap.keySet()) {
            FilterQuery filterQuery=new SimpleFacetQuery();
            Criteria filterCriteria=new Criteria("item_spec_"+key).is(specMap.get(key));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.6价格区间查询
      if ( searchMap.get("price")!=null&&!"".equals( searchMap.get("price"))){//按价格去筛选
          String[] prices = ((String) searchMap.get("price")).split("-");
            if (!"*".equals(prices[1])){
                FilterQuery filterQuery=new SimpleFacetQuery();
                Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(prices[0]);
                filterCriteria=filterCriteria.and("item_price").lessThanEqual(prices[1]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }else{
                FilterQuery filterQuery=new SimpleFacetQuery();
                Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(prices[0]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
      }


      //1.7设置分页参数。当前页码page每页显示条数
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageNo==null){
            pageNo=1;
        }
        if (pageSize==null){
            pageSize=10;
        }
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);


        //1.8排序
        String sortFiled = (String) searchMap.get("sortFiled");
        String sortValue = (String) searchMap.get("sortValue");

     if(!"".equals(sortFiled)){
         if("ASC".equals(sortValue)){
             Sort sort=new Sort(Sort.Direction.ASC,"item_"+sortFiled);
             query.addSort(sort);
         }else {
             Sort sort=new Sort(Sort.Direction.DESC,"item_"+sortFiled);
             query.addSort(sort);
         }

     }
        //所有数据
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);

        List<HighlightEntry<TbItem>> highlightEntryList = highlightPage.getHighlighted();//被高亮的部分
        for (HighlightEntry<TbItem> tbItemHighlightEntry : highlightEntryList) {

            TbItem entity = tbItemHighlightEntry.getEntity();//没被高亮的部分

            List<HighlightEntry.Highlight> highlights = tbItemHighlightEntry.getHighlights();//被高亮的部分  一个数据中有几个字段被高亮了
            //这里只有一个title被高亮了 ，要注意title不一定有高亮的内容
            if(highlights.size()>0){
                highlights.get(0).getField().getName();//获取到了item_title
                highlights.get(0).getSnipplets().get(0);//被高亮的数据
                entity.setTitle(  highlights.get(0).getSnipplets().get(0));//将高亮的部分设置到没被高亮的里边


            }
        }
        List<TbItem> rows = highlightPage.getContent();//之前把高亮的部分设置到了没被高亮的部分，所以现在获取就是有高亮的部分了
        itemMap.put("rows",rows);//展示数据
        itemMap.put("total",highlightPage.getTotalElements());//设置展示的总条数
        itemMap.put("totalPages",highlightPage.getTotalPages());//总页数

        return itemMap;
    }

    //2.查询分类数据，分组查询
    private List<String> findCategoryList(Map searchMap){
        List<String> categoryList=new ArrayList<>();
        //按照关键字查询，进行分组查询到分类数据
        Query query=new SimpleQuery();
        //按关键字去查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //设置分组

        GroupOptions groupOptions=new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> page = groupResult.getGroupEntries();
        List<GroupEntry<TbItem>> groupEntryList = page.getContent();
        for (GroupEntry<TbItem> tbItemGroupEntry : groupEntryList) {

            categoryList.add(  tbItemGroupEntry.getGroupValue());
        }


        return categoryList;

    }


    //3.
    private Map findBrandAndSpecList( String name){
        Map brandAndSpecMap=new HashMap();

            Long typeId= (Long) redisTemplate.boundHashOps("categoryList").get(name);

            if(typeId!=null){
                List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
                brandAndSpecMap.put("brandList",brandList);
                List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
                brandAndSpecMap.put("specList",specList);

            }
              return brandAndSpecMap;




    }

    @Override
    public void importItemDate(List<TbItem> itemList) {
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    @Override
    public void deleteItem(Long[] ids) {
        SolrDataQuery query=new SimpleFacetQuery();
        Criteria criteria=new Criteria("item_goodsid").in(ids);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


}
