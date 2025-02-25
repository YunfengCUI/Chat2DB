package ai.chat2db.server.web.api.controller.rdb;

import java.util.List;

import ai.chat2db.server.domain.api.param.DropParam;
import ai.chat2db.server.domain.api.param.ShowCreateTableParam;
import ai.chat2db.server.domain.api.param.TablePageQueryParam;
import ai.chat2db.server.domain.api.param.TableQueryParam;
import ai.chat2db.server.domain.api.param.TableSelector;
import ai.chat2db.server.domain.api.service.DatabaseService;
import ai.chat2db.server.domain.api.service.DlTemplateService;
import ai.chat2db.server.domain.api.service.TableService;
import ai.chat2db.server.tools.base.wrapper.result.ActionResult;
import ai.chat2db.server.tools.base.wrapper.result.DataResult;
import ai.chat2db.server.tools.base.wrapper.result.ListResult;
import ai.chat2db.server.tools.base.wrapper.result.PageResult;
import ai.chat2db.server.tools.base.wrapper.result.web.WebPageResult;
import ai.chat2db.server.web.api.aspect.ConnectionInfoAspect;
import ai.chat2db.server.web.api.controller.rdb.converter.RdbWebConverter;
import ai.chat2db.server.web.api.controller.rdb.request.DdlExportRequest;
import ai.chat2db.server.web.api.controller.rdb.request.TableBriefQueryRequest;
import ai.chat2db.server.web.api.controller.rdb.request.TableCreateDdlQueryRequest;
import ai.chat2db.server.web.api.controller.rdb.request.TableDeleteRequest;
import ai.chat2db.server.web.api.controller.rdb.request.TableDetailQueryRequest;
import ai.chat2db.server.web.api.controller.rdb.request.TableModifySqlRequest;
import ai.chat2db.server.web.api.controller.rdb.request.TableUpdateDdlQueryRequest;
import ai.chat2db.server.web.api.controller.rdb.vo.ColumnVO;
import ai.chat2db.server.web.api.controller.rdb.vo.IndexVO;
import ai.chat2db.server.web.api.controller.rdb.vo.SqlVO;
import ai.chat2db.server.web.api.controller.rdb.vo.TableVO;
import ai.chat2db.spi.model.Table;
import ai.chat2db.spi.model.TableColumn;
import ai.chat2db.spi.model.TableIndex;
import com.google.common.collect.Lists;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ConnectionInfoAspect
@RequestMapping("/api/rdb/table")
@RestController
public class TableController {

    @Autowired
    private TableService tableService;

    @Autowired
    private DlTemplateService dlTemplateService;

    @Autowired
    private RdbWebConverter rdbWebConverter;

    @Autowired
    private DatabaseService databaseService;


    /**
     * 查询当前DB下的表列表
     *
     * @param request
     * @return
     */
    @GetMapping("/list")
    public WebPageResult<TableVO> list(@Valid TableBriefQueryRequest request) {
        TablePageQueryParam queryParam = rdbWebConverter.tablePageRequest2param(request);
        TableSelector tableSelector = new TableSelector();
        tableSelector.setColumnList(false);
        tableSelector.setIndexList(false);
        PageResult<Table> tableDTOPageResult = tableService.pageQuery(queryParam, tableSelector);
        List<TableVO> tableVOS = rdbWebConverter.tableDto2vo(tableDTOPageResult.getData());
        return WebPageResult.of(tableVOS, tableDTOPageResult.getTotal(), request.getPageNo(),
            request.getPageSize());
    }


    /**
     * 查询当前DB下的表columns
     * d
     *
     * @param request
     * @return
     */
    @GetMapping("/column_list")
    public ListResult<ColumnVO> columnList(@Valid TableDetailQueryRequest request) {
        TableQueryParam queryParam = rdbWebConverter.tableRequest2param(request);
        List<TableColumn> tableColumns = tableService.queryColumns(queryParam);
        List<ColumnVO> tableVOS = rdbWebConverter.columnDto2vo(tableColumns);
        return ListResult.of(tableVOS);
    }

    /**
     * 查询当前DB下的表index
     *
     * @param request
     * @return
     */
    @GetMapping("/index_list")
    public ListResult<IndexVO> indexList(@Valid TableDetailQueryRequest request) {
        TableQueryParam queryParam = rdbWebConverter.tableRequest2param(request);
        List<TableIndex> tableIndices = tableService.queryIndexes(queryParam);
        List<IndexVO> indexVOS = rdbWebConverter.indexDto2vo(tableIndices);
        return ListResult.of(indexVOS);
    }

    /**
     * 查询当前DB下的表key
     *
     * @param request
     * @return
     */
    @GetMapping("/key_list")
    public ListResult<IndexVO> keyList(@Valid TableDetailQueryRequest request) {
        // TODO 增加查询key实现
        return ListResult.of(Lists.newArrayList());
    }

    /**
     * 导出建表语句
     *
     * @param request
     * @return
     */
    @GetMapping("/export")
    public DataResult<String> export(@Valid DdlExportRequest request) {
        ShowCreateTableParam param = rdbWebConverter.ddlExport2showCreate(request);
        return tableService.showCreateTable(param);
    }

    /**
     * 建表语句样例
     *
     * @param request
     * @return
     */
    @GetMapping("/create/example")
    public DataResult<String> createExample(@Valid TableCreateDdlQueryRequest request) {
        return tableService.createTableExample(request.getDbType());
    }

    /**
     * 更新表语句样例
     *
     * @param request
     * @return
     */
    @GetMapping("/update/example")
    public DataResult<String> updateExample(@Valid TableUpdateDdlQueryRequest request) {
        return tableService.alterTableExample(request.getDbType());
    }

    /**
     * 获取表下列和索引等信息
     *
     * @param request
     * @return
     */
    @GetMapping("/query")
    public DataResult<TableVO> query(@Valid TableDetailQueryRequest request) {
        TableQueryParam queryParam = rdbWebConverter.tableRequest2param(request);
        TableSelector tableSelector = new TableSelector();
        tableSelector.setColumnList(true);
        tableSelector.setIndexList(true);
        DataResult<Table> tableDTODataResult = tableService.query(queryParam, tableSelector);
        TableVO tableVO = rdbWebConverter.tableDto2vo(tableDTODataResult.getData());
        return DataResult.of(tableVO);
    }

    /**
     * 获取修改表的sql语句
     *
     * @param request
     * @return
     */
    @GetMapping("/modify/sql")
    public ListResult<SqlVO> modifySql(@Valid TableModifySqlRequest request) {
        return tableService.buildSql(
                rdbWebConverter.tableRequest2param(request.getOldTable()),
                rdbWebConverter.tableRequest2param(request.getNewTable()))
            .map(rdbWebConverter::dto2vo);
    }

    /**
     * 删除表
     *
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public ActionResult delete(@Valid @RequestBody TableDeleteRequest request) {
        DropParam dropParam = rdbWebConverter.tableDelete2dropParam(request);
        return tableService.drop(dropParam);
    }
}
