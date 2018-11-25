package com.pyg.page.service.impl;

import com.pyg.mapper.TbGoodsDescMapper;
import com.pyg.mapper.TbGoodsMapper;
import com.pyg.mapper.TbItemCatMapper;
import com.pyg.mapper.TbItemMapper;
import com.pyg.page.service.ItemPageService;
import com.pyg.pojo.TbGoods;
import com.pyg.pojo.TbGoodsDesc;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;
    @Autowired
    private TbGoodsMapper tbGoodsMapper;
    @Autowired
    private TbGoodsDescMapper tbGoodsDescMapper;
    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Value("${pageDir}")
    private String pageDir;

    @Override
    public void genItemPage(Long goodsId) throws Exception {
        //创建configuration 的模板
        Configuration configuration = freeMarkerConfig.getConfiguration();
        Template template = configuration.getTemplate("item.ftl");

        //准备数据
        Map dataModel=new HashMap();
//        tbgoods  表
        TbGoods goods = tbGoodsMapper.selectByPrimaryKey(goodsId);
        dataModel.put("goods",goods);
        TbGoodsDesc goodsDesc =tbGoodsDescMapper.selectByPrimaryKey(goodsId);
        dataModel.put("goodsDesc",goodsDesc);
        //面包屑
        String itemCat1 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
        String itemCat2 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
        String itemCat3 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
        dataModel.put("itemCat1",itemCat1);
        dataModel.put("itemCat2",itemCat2);
        dataModel.put("itemCat3",itemCat3);

//        tb_item  的数据sku列表
        TbItemExample example=new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        criteria.andGoodsIdEqualTo(goodsId);
//        库存是否大于0
        criteria.andNumGreaterThan(0);
//        按照是否默认进行降序排序
        example.setOrderByClause("is_default DESC");
        List<TbItem> itemList = tbItemMapper.selectByExample(example);
        dataModel.put("itemList",itemList);

        //调用模板引擎，生成HTML文件
        Writer out=new FileWriter(pageDir + goodsId + ".html");
        template.process(dataModel,out);
        out.close();
    }

    @Override
    public void deletePageById(Long goodsId) {
        String path=pageDir + goodsId + ".html";
        new File(path).delete();
    }

}
