package cn.qihangerp.api.controller.goods;

import cn.qihangerp.interfaces.goods.bo.GoodsSpecBo;
import cn.qihangerp.common.BaseController;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.interfaces.goods.domain.ErpGoods;
import cn.qihangerp.interfaces.goods.domain.ErpGoodsSpec;
import cn.qihangerp.interfaces.goods.service.ErpGoodsSpecService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/goods/goodsSpec")
@RestController
public class GoodsSpecController extends BaseController {
    @DubboReference
    private ErpGoodsSpecService goodsSpecService;
    @GetMapping("/list")
    public TableDataInfo list(GoodsSpecBo goods, PageQuery pageQuery)
    {
        PageResult<ErpGoodsSpec> pageResult = goodsSpecService.queryPageList(goods, pageQuery);
        return getDataTable(pageResult);
    }


}
