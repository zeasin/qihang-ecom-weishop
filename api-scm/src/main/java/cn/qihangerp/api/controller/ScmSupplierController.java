package cn.qihangerp.api.controller;


import cn.qihangerp.common.BaseController;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.interfaces.purchase.domain.ScmSupplier;
import cn.qihangerp.interfaces.purchase.service.ScmSupplierService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 【请填写功能名称】Controller
 * 
 * @author qihang
 * @date 2023-12-29
 */
@RestController
@RequestMapping("/scm/supplier")
public class ScmSupplierController extends BaseController
{
    @DubboReference
    private ScmSupplierService scmSupplierService;

    /**
     * 查询【请填写功能名称】列表
     */
    @GetMapping("/list")
    public TableDataInfo list(ScmSupplier scmSupplier)
    {
        List<ScmSupplier> list = scmSupplierService.list();
        return getDataTable(list);
    }


}
