package com.pyg.sellergoods.service;

import entity.PageResult;
import com.pyg.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {

    public List<TbBrand> findAll();

    public PageResult findPage(int pageNum,int pageSize);

    public void add(TbBrand brand);


    //回显
    public TbBrand findOne(Long id);

    public void update(TbBrand brand);

    public void delete(Long [] ids);

    public PageResult findPage(TbBrand brand,int pageNum,int pageSize);

    //查询品牌下拉列表
    public  List<Map> selectOptionList();
}
