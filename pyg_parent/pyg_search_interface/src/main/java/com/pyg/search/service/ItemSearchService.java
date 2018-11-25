package com.pyg.search.service;

import com.pyg.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {


 //返回值是后端给前端的，条件是后端给前端的
    public Map<String,Object> search(Map searchMap);

    public void importItemDate(List<TbItem> itemList);

    public void deleteItem(Long[]ids);
}
