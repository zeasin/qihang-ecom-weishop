package cn.qihangerp.api.controller.goods;

import cn.qihangerp.common.BaseController;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.interfaces.goods.domain.ErpGoods;
import cn.qihangerp.interfaces.goods.service.ErpGoodsService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/goods/goods")
@RestController
public class GoodsController extends BaseController {
    @DubboReference
    private ErpGoodsService goodsService;
    @GetMapping("/list")
    public TableDataInfo list(ErpGoods goods, PageQuery pageQuery)
    {
        PageResult<ErpGoods> pageResult = goodsService.queryPageList(goods, pageQuery);
        return getDataTable(pageResult);
    }


}
