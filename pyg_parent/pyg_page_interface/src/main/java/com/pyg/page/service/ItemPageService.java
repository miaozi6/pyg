package com.pyg.page.service;

public interface ItemPageService {


    //根据商品的spu，生成商品的静态页面
    public void genItemPage(Long goodsId) throws Exception;


    void deletePageById(Long id);
}
