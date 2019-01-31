package hncz.soft.midplain.budget.action;

import hncz.soft.midplain.basedata.vo.CaUserVO;
import hncz.soft.midplain.basinfor.bo.BasicDataEntryService;
import hncz.soft.midplain.budget.bo.BudgetEditService;
import hncz.soft.midplain.budget.vo.BudgetpromainVO;
import hncz.soft.midplain.common.action.PagedAction;
import hncz.soft.midplain.common.bo.CommonService;
import hncz.soft.midplain.common.bo.ReportService;
import hncz.soft.midplain.common.consts.SystemConst;
import hncz.soft.midplain.common.dao.DefaultDAO;
import hncz.soft.midplain.common.exception.OTSException;
import hncz.soft.midplain.common.xml.XmlUtil;
import hncz.soft.midplain.lmtset.bo.SysRoleAgencyService;
import hncz.soft.midplain.projmanager.bo.DocService;
import hncz.soft.midplain.projmanager.bo.ProjManagerService;
import hncz.soft.midplain.quota.vo.QuotaFomVO;
import hncz.soft.midplain.systables.vo.SysTablesVO;
import hncz.soft.midplain.trans.bo.UnitRepStaService;
import hncz.soft.midplain.useset.bo.ForMulaRefExeService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts2.ServletActionContext;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.yworks.b.c;

/**
 * 
 * 说明：预算编报ACTION
 * @version 1.0
 */
@SuppressWarnings("serial")
public class AdjustBudgetEditAction extends PagedAction  {
	/**
     * 在这里只需要声明使用到的业务逻辑层对象,其实例由spring负责创建
     */
	//前台传递的TABLENAME动态变化
	private String tablename;	
	@Autowired
    private ReportService reportService;
	@Autowired
    private BudgetEditService	budgetEditService;
	@Autowired
    private ProjManagerService	projManagerService;
	
	@Autowired
	 private SysRoleAgencyService sysRoleAgencyService; 
	@Autowired
	private BasicDataEntryService basicDataEntryService;
	@Autowired
    private CommonService commonService;
	@Autowired
    private UnitRepStaService unitRepStaService;
	@Autowired
    private ForMulaRefExeService forMulaRefExeService;
	// 数据访问层对象
	@Autowired
	private DefaultDAO dao;
    @Autowired
	private DocService docService;
	
    public String getTablename() {
		return tablename;
	}
	public void setTablename(String tablename) {
		this.tablename = tablename;
	}
	public AdjustBudgetEditAction() {
        super();
    }
    // 显示首页
    public String showIndex() throws Exception {
    	String CURRENTYEAR = commonService.getParameter((List)session.get(SystemConst.SESSION_SYSPARA), "CURRENTYEAR");
		request.setAttribute("currentyear", CURRENTYEAR);
		Map<String,Object> map = (Map<String, Object>) request.getSession().getAttribute("BUDGETEND");
		request.setAttribute("budgetend", map.get("BUDGETEND"));
        return INDEX;
    } 

    // 显示通用report界面
    public String showPubIndex() throws Exception {
        return "pubindex";
    }    
    // 显示明细表界面
    public String showReport() throws Exception {
    	tablename = request.getParameter("tablename");
        return "detailreport";
    }
    //点击新增以后，明细表跳转方法
    public String showAddReport() throws Exception {
    	String CURRENTYEAR = commonService.getParameter((List)session.get(SystemConst.SESSION_SYSPARA), "CURRENTYEAR");
		request.setAttribute("currentyear", CURRENTYEAR);
    	request.setAttribute("add", true);
    	return "detailreport";
    }
    // 显示项目输入首页
    public String showAddIndex() throws Exception {
    	/**
    	 * TB_BUSI_BUDGETPROMAIN  （前台传）单位id,项目id，（后台查询）项目类别，功能科目 
    	 */
    	CaUserVO cuv=(CaUserVO)session.get("session_user");
    	String tablename = request.getParameter("tablename");
    	String projId = request.getParameter("projId");
    	String CURRENTYEAR = commonService.getParameter((List)session.get(SystemConst.SESSION_SYSPARA), "CURRENTYEAR");
		//request.setAttribute("currentyear", CURRENTYEAR);
    	if("TB_BUSI_ZFCGYSLR".equalsIgnoreCase(tablename)){
    		Object retObj = this.budgetEditService.queryProjObject(null, "ProjManager", "queryZFCG");
    		tablename = retObj.toString();
    		SysTablesVO stv = new SysTablesVO();
    		stv.setPhysicalname(tablename);
    		Object tableid = this.dao.queryObject(stv, "SysTables", "querytableid");
    		request.setAttribute("tablename", tablename);
    		request.setAttribute("tableid", tableid);
    		request.setAttribute("projId", projId);
    		return "pubreport";
    		
    	}
    	if("TB_PUBPROREPTXT".equalsIgnoreCase(tablename)){
    		//查询项目申报书的信息
    		//通过项目查询项目类别
    		Map map = new HashMap();
    		map.put("bpguid", projId);
    		HttpSession session = request.getSession();      
    		ServletContext  application  = session.getServletContext();    
    		String serverRealPath = application.getRealPath("/") ;
    		String path = serverRealPath+"doc";
    		String swfToolsPath = serverRealPath+"SWFTools";
    		docService.setDocDir(path);
    		docService.setSwfToolsPath(swfToolsPath);
			String swfFileName = docService.createDocByTemplate(projId,cuv);
    		String name = "";//docService.createDocByTemplate("10");
    		request.setAttribute("swfFileName", request.getContextPath()+swfFileName.replace("\\", "/"));
    		request.setAttribute("tablename", tablename);
    		request.setAttribute("projId", projId);
    		return "pubproreptxt";
    	}
    	if(null!=tablename&&!"".equals(tablename)){
    		SysTablesVO stv = new SysTablesVO();
    		stv.setPhysicalname(tablename);
    		Object tableid = this.dao.queryObject(stv, "SysTables", "querytableid");
    		request.setAttribute("tablename", tablename);
    		request.setAttribute("tableid", tableid);
    		request.setAttribute("projId", projId);
        	return "pubreport";
    	}
        return "budgetpromain";
    }

    /**
     * 根据项目类别查询经济分类
     * @return
     * @throws OTSException 
     * @throws IOException 
     */
    @SuppressWarnings("unchecked")
	public String queryEpe() throws OTSException, IOException{
    	String petcode = request.getParameter("petcode");
    	Map map = new HashMap();
    	map.put("petcode", petcode);
    	List list = this.budgetEditService.queryProjInputList(map, "ProjManager", "queryEpe");
    	this.responseInterface(list);
    	return null;
    }
    /**
     * 根据项目类别查询功能科目
     * @return
     * @throws OTSException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
	public String queryEp() throws OTSException, IOException{
    	String petcode = request.getParameter("petcode");
    	String aguid = request.getParameter("aguid");
    	Map map = new HashMap();
    	map.put("petcode", petcode);
    	map.put("aguid", aguid);
    	List list = this.budgetEditService.queryProjInputList(map, "ProjManager", "queryEp");
    	if(list.size()==0){
    		list = this.budgetEditService.queryProjInputList(map, "ProjManager", "queryAllEp");
    	}
    	this.responseInterface(list);
    	return null;
    }
    /**
     * 查询项目类别
     * @return
     * @throws OTSException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
	public String queryPetcode() throws OTSException, IOException{
    	String ispro = request.getParameter("ispro");
    	Map map = new HashMap();
    	map.put("ispro", ispro);
    	List list = this.budgetEditService.queryProjInputList(map, "ProjManager", "queryPetcode");
    	
    	this.responseInterface(list);
    	return null;
    }
    /**
     * 根据单位查询归口处室
     * @return
     * @throws OTSException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
	public String querymd() throws OTSException, IOException{
    	String aguid = request.getParameter("aguid");
    	Map map = new HashMap();
    	map.put("aguid", aguid);
    	List list = this.budgetEditService.queryProjInputList(map, "ProjManager", "queryMd");
    	this.responseInterface((Map)list.get(0));
    	return null;
    }
    // 保存项目录入
    public String doInsert() throws Exception {
        return NONE;
    }
    /**
     * 查询项目关联的表信息(多个TAB)
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public String queryTabpanel() throws Exception{
    	//查询配置表是否可见
    	Map map = new HashMap();
    	String bpguid = request.getParameter("projId");
    	map.put("bpguid", bpguid);
    	List codelist = this.budgetEditService.queryProjInputList(map, "ProjManager", "queryPetcodeById");
    	List list = new ArrayList();
    	if(null!=codelist && codelist.size()>0){
    		list = this.budgetEditService.queryProjInputList(codelist.get(0), "ProjManager", "queryProjTabByType");
        	
    	}   
    	super.responseInterface(list);
    	return null;
    }
    /**
     * 返回有数据级次在本级的所有单位
     * 
     *
     * @return
     * @throws Exception
     */
	public void checkdatasta() throws Exception {
    	String acode=request.getParameter("acode");
    	CaUserVO cuv=(CaUserVO)session.get("session_user");
    	String aguidstr=projManagerService.checkdatasta(acode,cuv);
    	/*super.responseInterface(aguidstr);
    	return null;*/
    	newCheckData(aguidstr,"1","0");
	}
	/** 
     * @Title: newCheckData 
     * @Description: 校验所传单位的数据
     * @param sendaguid 需要进行校验的单位串
     * @param busitype 业务类型，需要用到的模块传该参数，如校验全部，传0,1,2,3,4
     * @param change 是否更改校验标志，一般模块都不会校验单位全部表，所以写0 否
     * @param identity 登陆用户身份，用来筛选校验公式的。0 财政，1 部门，2非底级单位，3底级单位
     * @return List   返回类型 
     * @throws OTSException
     */
    @SuppressWarnings("unchecked")
	public String newCheckData(String sendaguid, String busitype, String change) throws Exception{
    	 //CaUserVO CUV = (CaUserVO)session.get("session_user");
    	CaUserVO CUV = (CaUserVO)session.get(SystemConst.SESSION_USER);
    	String year = commonService.getParameter((List)session.get(SystemConst.SESSION_SYSPARA), "CURRENTYEAR");
    	int resptype = 0;
    	String respvalue = "wx,";
    	
    	if(null==sendaguid || sendaguid.equals("")){
    		try {
				super.responseInterface("-1");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
    	}
    	
    	String identity = "";
		try {
			identity = unitRepStaService.getidentity(CUV);
		} catch (OTSException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	List sumlist = new ArrayList();
    	try {
    		sumlist = unitRepStaService.doSaveCheck(sendaguid, busitype, change, identity,year);
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if(sumlist.size()>0){
    		Map map = new HashMap();
    		map = (Map)sumlist.get(0);
    		if(null != map.get("status")){
    			if(map.get("status") == "0"){
    				//sumlist = new String("wx");
    				respvalue += String.valueOf(map.get("checkdefid"));
    				resptype = 1;
    			}
    		}
    	}
    	
    	try {
    		if(resptype == 0){
    			super.responseInterface(sumlist);
    		}else if(resptype == 1){
    			super.responseInterface(respvalue);
    		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
	}
    /**
     * 
     * @return null
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public String showBuildXML() throws Exception {
    	Map map = new HashMap();
    	String tablename1 = request.getParameter("tablename1");
    	String tablename2 = request.getParameter("tablename2");
    	map.put("tablename1", tablename1);
    	map.put("tablename2", tablename2);
    	String retXml = reportService.getReportTitle(map);
    	
    	super.responseInterface(retXml);
        return null;
    }
    /**
     * 查询定额公式返回数据级别
     * @return
     * @throws OTSException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
	public String queryValidateFuncValue() throws OTSException, IOException{
    	String validate = request.getParameter("validateColumn");
    	String petcode = request.getParameter("petcode");
    	String epecode = request.getParameter("epecode");
    	String aguid = request.getParameter("aguid");
    	Map map =  new HashMap();
    	map.put("PETCODE", petcode);
    	map.put("EPECODE", epecode);
    	map.put("AGUID", aguid);
    	String string = this.budgetEditService.queryValidateFuncValue(map);
    	Map retMap = new HashMap();
    	retMap.put("str", string);
    	this.responseInterface(retMap);
    	return null;
    }
    @SuppressWarnings("unchecked")
	public String queryExpressByColName() throws OTSException, IOException{
    	String physicalname = request.getParameter("physicalname");
    	String aguid = request.getParameter("aguid");
    	String petcode = request.getParameter("petcode");
    	String epecode = request.getParameter("epecode");
    	Map map = new HashMap();
    	map.put("physicalname", physicalname.toLowerCase());
    	map.put("AGUID", aguid);
    	map.put("PETCODE", petcode);
    	map.put("EPECODE", epecode);
    	
    	String str = this.getValidateColumnValue(map);
    	//List list = this.projManagerService.queryProjInputList(map, "ProjManager", "queryExpressByColName");
    	String[] valStr = str.split(",");
    	System.out.println("********"+valStr[0]+"***********");
    	Map paramMap = new HashMap();
    	
    	if(!str.equals("")){
    		for(int i=0;i<valStr.length;i++){
        		String[] colStr = valStr[i].split("=");
        		paramMap.put(colStr[0],colStr[1] );
        	}
    	}
    	
    	
    	if(null!=paramMap.get(physicalname)&&!"1".equals(paramMap.get(physicalname).toString())&&!"2".equals(paramMap.get(physicalname).toString())){
        	this.responseInterface("0");
    	}else{
    		this.responseInterface("1");
    	}
    	return null;
    }
    /**
     * 项目明细表表头处理类
     * @return null
     * @throws Exception
     */    
    @SuppressWarnings({"unused","unchecked"})
	public String showProjXML() throws Exception {
    	Map map = new HashMap();
		String id = request.getParameter("id");
		String petcode = request.getParameter("petcode");
		String projId = request.getParameter("mainId");
		
    	String tablename = request.getParameter("tablename");
    	String aguid = request.getParameter("aguid");
    	String edit = request.getParameter("edit")==null?"true":request.getParameter("edit");
    	
    	CaUserVO cuv=(CaUserVO)session.get("session_user");
    	String tableid = budgetEditService.getTableId(tablename);
    	
    	String retXml=budgetEditService.showBudgetDetailTitle(petcode,tableid,tablename,cuv,aguid,projId,edit); 
     	super.responseInterface(retXml);
        return null;
    }
    /*
     * 按跨表公式修改数据  然后 查询结果返回至页面
     */
	@SuppressWarnings("unchecked")
	public String updataload() throws Exception{
		CaUserVO cuv=(CaUserVO)session.get("session_user");
    	String aguid=request.getParameter("aguid");
    	String acode = request.getParameter("acode");
    	String sendguid = "";
    	String sendtableid ="";
    	sendguid=projManagerService.checkdatasta(acode,cuv);
		Map map2 = new HashMap();
		String sql = "select * from tb_systables t where (t.busitype='0' or t.isgovpch='1' or t.tableid='001') and t.isused='1'";
    	map2.put("sql",sql);
    	List projTableList = this.budgetEditService.queryProjInputList(map2,"pub_default_sql","default_query_sql");
    	String changestatus = "";
    	for(int i=0;i<projTableList.size();i++){
    		Map projTabMap  = (Map)projTableList.get(i);
    		if("".equals(sendtableid)){
    			sendtableid = projTabMap.get("TABLEID").toString();
    		}else{
    			sendtableid = sendtableid + "," + projTabMap.get("TABLEID").toString();
    		}
    	}  
		
    	//String CURRENTYEAR = commonService.getParameter((List)session.get(SystemConst.SESSION_SYSPARA), "CURRENTYEAR"); 
    	String CURRENTYEAR = request.getParameter("year");
    	
    	List sumlist = new ArrayList();

		if(null == sendguid || sendguid.equals("")){
    		try {
    			Map retMap = new HashMap();
    			retMap.put("message", "-1");
				super.responseInterface(retMap);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
    	}
		String[] tableidarray = sendtableid.split(",");
    	String[] sendaguidarray = sendguid.split(",");

    	sendtableid = "";
    	sendguid = "";

    	for(int i=0; i<sendaguidarray.length; i++){
	    	for(int j=0; j<tableidarray.length; j++){
	    		sendtableid += sendtableid.equals("")?tableidarray[j]:","+tableidarray[j];
	    		sendguid += sendguid.equals("")?sendaguidarray[i]:","+sendaguidarray[i];

	    		
	    	}
    	}
    	try {
    		sumlist = forMulaRefExeService.doSaverefresh(sendtableid, sendguid, "0","", "", "", CURRENTYEAR,"");
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	try {
    		Map retMap = new HashMap();
    		
	    	if(sumlist.size()==0){
	    		retMap.put("message", "1");
					super.responseInterface(retMap);
	    	}else{
	    		Map map = new HashMap();
	    		map = (Map)(sumlist.get(0));
					try {
						retMap.put("message", "刷新【" + forMulaRefExeService.guidtoname(map.get("aguid").toString()) + "】的【" +  forMulaRefExeService.tableidtoname(map.get("tableid").toString()) +  "】中"+map.get("formulatype").toString()+"【" + map.get("colname") + "】列时 失败");
						super.responseInterface(retMap);
					} catch (OTSException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    	}
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        return null;
    }
    /*
     * Load 数据前判断 跨表公式字段的值 与跨表公式sql查询的结果是否相等，不相等则说明 跨表公式源表数据发生改变
     * 提示用户是否刷新数据保证数据同步 
     */
    @SuppressWarnings("unchecked")
	public String judgment() throws Exception{
    	CaUserVO cuv=(CaUserVO)session.get("session_user");
    	//String currentyear = commonService.getParameter((List)session.get(SystemConst.SESSION_SYSPARA), "CURRENTYEAR");
    	String currentyear = request.getParameter("year");
    	
    	Map cuvmap=new HashMap();
    	cuvmap.put("userid", cuv.getUserid());
    	cuvmap.put("orgcode", cuv.getOrgcode());
    	cuvmap.put("orgtype", cuv.getOrgtype());
    	cuvmap.put("currentyear", currentyear);
    	
    	String aguid=request.getParameter("aguid");
    	String acode = request.getParameter("acode");
    	//this.projManagerService.checkdatasta(aguid, cuv);
    	Map map=new HashMap();
    	//把项目类的表全部取到
    	String sql = "select * from tb_systables t where (t.busitype='0' or t.isgovpch='1' or t.tableid='001') and t.isused='1' and t.sysflag in ('0','1')";
    	map.put("sql",sql);
    	List projTableList = this.projManagerService.queryProjInputList(map,"pub_default_sql","default_query_sql");
    	map.remove("sql");
    	//查询所有的定额公式
    	map.put("aguid", aguid);
    	List quotafomList = this.projManagerService.queryProjInputList(map, "ProjManager", "queryAllQuotafom");
    	for(int i=0;i<projTableList.size();i++){
    		Map projTabMap  = (Map)projTableList.get(i);
    		//判断跨表公式 源数据值是否发生改变
    		Map map1 = this.projManagerService.judgmentService(cuvmap,cuv,projTabMap.get("TABLEID").toString(),projTabMap.get("PHYSICALNAME").toString(),projTabMap.get("TABTITLE").toString(),aguid,quotafomList,acode);
    		if("1".equals(map1.get("changestatus").toString())){
    			map.put("changestatus", 1);
    			break;
    		}
    	}    	
    	super.responseInterface(map);
    	return null;
    }
    /*
     * 根据 单位、tableid 获取表中跨表公式列 将所有公式列拼成key value形式返货页面 由页面解析后向单元格赋值
     *
     */
    @SuppressWarnings("unchecked")
	public String crosstabcol() throws Exception{
    	String aguid=request.getParameter("aguid");
    	String tableid=request.getParameter("tableid");
    	String petcode = request.getParameter("petcode");
    	String str="";
		try {
			str = this.budgetEditService.crosstabcol1(aguid,tableid,petcode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map map = new HashMap();
		map.put("str", str);
    	super.responseInterface(map);
    	return null;
    }
    /**
     * 页面点击校验操作的方法
     * @return
     * @throws IOException
     * @throws OTSException
     */
    @SuppressWarnings("unchecked")
	public String  audit() throws IOException, OTSException{
    	CaUserVO CUV=(CaUserVO)session.get("session_user");
    	String guid=request.getParameter("aguid");
    	List list=this.budgetEditService.auditFormula(CUV,guid);
    	super.responseInterface(list);
    	return null;
    }
    /**
     * 点击机构树展示的TREELIST列表界面 包括主表和明细表的表头信息
     * @return null
     * @throws Exception
     */
	public String showProjTreeListXML() {
		try {
			CaUserVO CUV=(CaUserVO)session.get("session_user");
			String retXml = this.projManagerService.buildTwoTableTreeListTitle(CUV);
	     	super.responseInterface(retXml);
		} catch (Exception e) {
			e.printStackTrace();
			logger.equals(e.getMessage());
		}
		
    	return null;
    }
	 /**
     * 查询主表和明细表数据
     * @return json
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public String showProjTreeListData() throws Exception{
    	String agencyid = request.getParameter("agencyid");
    	String year = request.getParameter("year");
    	String isChange = request.getParameter("isChange");
    	CaUserVO CUV=(CaUserVO)session.get("session_user");
    	if(agencyid.equals("0")){
    		List projList = this.budgetEditService.queryProjTreeListData(CUV.getUserid().toString(),CUV.getOrgcode().toString(),CUV.getOrgtype().toString(),year,isChange);
    		this.responseInterface(projList);
    		return null;
    	}
    	if(null!=agencyid && !"".equals(agencyid) && !"null".equals(agencyid)){
    		List projTypeList = this.budgetEditService.queryProjTreeListData(agencyid,year,isChange);
    		this.responseInterface(projTypeList);
    	}		
    	return null;
    }
    
    /**
     * 调整预算编审下数据
     * */
    public void doChange() throws Exception{
    	String dataid = request.getParameter("dataid");
    	String tableid = request.getParameter("tableid");
    	String tabName = request.getParameter("tabName");
    	this.basicDataEntryService.doChange(dataid,tableid,tabName);
    }
    
    
    
    /**
     * 查询表数据(Tab页面数据)
     * @return JSON
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public String showBuildData() throws Exception {
    	tablename = request.getParameter("tablename");
    	String mainId = request.getParameter("mainId"); 
    	//处理大字段LXL
		String cols="";//要查的字段
		HashMap colInfos=getColInfo(tablename);//得到数据库表字段类型以便转换输出 clob date
		Set set=colInfos.entrySet(); 
	    Iterator it=set.iterator(); 
	    while(it.hasNext()){ 
	         Map.Entry me=(Map.Entry)it.next(); 
	         if(me.getValue().equals("DATE")){
	        	 cols+="to_char("+me.getKey()+",'yyyy-mm-dd') as "+me.getKey()+",";
	         }else if(me.getValue().equals("CLOB")){
	        	 cols+="to_char("+me.getKey()+") as "+me.getKey()+",";
	         }else{
	        	 cols+=me.getKey()+",";
	         }
	    } 
	    cols=cols.substring(0, cols.length()-1);
	    String sql="select "+cols+" from "+tablename+" where bpguid='"+mainId+"'";
		Map querymap=new HashMap();
		querymap.put("sql", sql);
    	List list= this.dao.queryList(querymap, "pub_default_sql", "default_query_sql");
	   /* Map map = new HashMap();
    	map.put("tablename", tablename);
    	map.put("mainId", mainId);
    	List list = this.budgetEditService.queryProjInputList(map, "ProjManager", "queryTabData");    */
    	super.responseInterface(list);
        return null;
    }    
    /**
     * 查询主表对应明细表数据
     * @return json
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public String showDetailData() throws Exception {
    	
    	HashMap map = new HashMap();
    	String mainId = request.getParameter("mainId"); 
    	map.put("mainId", mainId);
    	List list = this.budgetEditService.queryProjInputList(map, "ProjManager", "queryDetailData"); 
    	//重组list添加横向验证公式
    	rebuildListForValidate(list);
    	super.responseInterface(list);
    	
    	return null;
    }
    /**
     * 重新组装新增字段的validateColumn
     * @param list
     * @throws OTSException
     */
	@SuppressWarnings("unchecked")
	private void rebuildListForValidate(List list) throws OTSException {
		for(int i=0;i<list.size();i++){
    		Map rebuildMap = (Map)list.get(i);
    		/**
    		 * 1.查询tb_pubexptypeeco表，tb_pubexptypeecocsour表  ETEID是否有关联数据
    		 * 如果有关联关系则说明有资金来源，如果没有关联关系则判断isleaf为0可录1为不可录
    		 * 2.查询tb_pubexptypeecocsour表，tb_pubexptypeecofom表isdefault是否为1，
    		 * 如果有则根据单位查询对应公式
    		 * 3.tb_pubexptypeecofomunit表，单位和公式的对应关系为多对一，即一个公式对应多个单位
    		 * 4.tb_pubquotafom公式表
    		 * 5.tb_pubprocapitalsource资金来源表
    		 * 6.
    		 */
    		String validateColumn = getValidateColumnValue(rebuildMap);
    		rebuildMap.put("validateColumn", validateColumn);
    	}
	}
	/**
	 * 前台点击新增行调用查询单元格验证信息
	 * @return null
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String queryValidateColumn() throws Exception{
		String PETCODE = request.getParameter("petcode");
		String EPECODE = request.getParameter("epecode");
		String AGUID = request.getParameter("aguid");
		
		Map rebuildMap = new HashMap();
		rebuildMap.put("PETCODE", PETCODE);
		rebuildMap.put("EPECODE", EPECODE);
		rebuildMap.put("AGUID", AGUID);
		
		String validateColumn = getValidateColumnValue(rebuildMap);
		//rebuildMap.put("validateColumn", validateColumn);
		this.responseInterface(validateColumn);
		return null;
	}
	/**
	 * 获取新增列验证权限的方法过程
	 * @param rebuildMap
	 * @return null
	 * @throws OTSException
	 */
	@SuppressWarnings("unchecked")
	private String getValidateColumnValue(Map rebuildMap) throws OTSException {
		List validateList1 = this.budgetEditService.queryProjInputList(rebuildMap, "ProjManager", "queryValidateDatasByIsleaf");
		List validateList2;
		if(validateList1.size()>0){
			validateList2 = this.budgetEditService.queryProjInputList(rebuildMap, "ProjManager", "queryDefaultValidateDatas");
		}else{
			validateList2 = this.budgetEditService.queryProjInputList(rebuildMap, "ProjManager", "queryIsleaf");
		}    		   		
		
		List validateList3 = this.budgetEditService.queryProjInputList(rebuildMap, "ProjManager", "queryValidateDatas");
		//重组list
		String validateColumn="";
		if(validateList3.size()<=0){
			validateList3 = validateList2;
		} 
		for(int j=0;j<validateList3.size();j++){
			Map validateMap = (Map)validateList3.get(j);
			//把资金来源所有的验证拼接字符串
			if("".equals(validateColumn)){
				validateColumn = validateMap.get("PHYSICALNAME")+"="+ validateMap.get("QFID");
			}else{
				validateColumn = validateColumn +","+validateMap.get("PHYSICALNAME")+"="+ validateMap.get("QFID");
			}    			
		}
		return validateColumn;
	}

	/**
	 * 通过qid取得对应的定额公式值
	 * @param rebuildMap
	 * @return null
	 * @throws OTSException
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public String queryValidateColumnValue() throws OTSException, IOException {
		String aguid = request.getParameter("aguid");
		String qid = request.getParameter("qid");
		
		QuotaFomVO qfv = new QuotaFomVO();
		qfv.setQfid(Long.valueOf(qid));
		List list  = this.dao.queryList(qfv, "QuotaFom", "query");
		QuotaFomVO qf = (QuotaFomVO)list.get(0);
		Map paraMap = new HashMap();
		paraMap.put("sql", qf.getQfsql().replace("【WHERE】", " and aguid='"+aguid+"'"));
		Object retValue = this.dao.queryObject(paraMap, "pub_default_sql", "query_guid");
		paraMap.clear();
		paraMap.put("obj", retValue.toString());
		this.responseInterface(paraMap);
		return null;
	}
	 

	// 拼装项目录入机构项目树形展示界面
	public String treexml() throws Exception {
		CaUserVO user = (CaUserVO) session.get("session_user");
		String lefttitlexml = "";
		try {
			lefttitlexml = sysRoleAgencyService.getRoleAgcTreeXML(user
					.getUserid(), user.getOrgtype(), user.getOrgcode());
			super.responseInterface(lefttitlexml);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return null;
    }
    
 // 根据机构id查询项目主表信息
    @SuppressWarnings("unchecked")
	public String queryBudgetMain() {
    	try{
    		Map map = new HashMap();
        	String agencyid = request.getParameter("agencyid");
        	String year = request.getParameter("year");//年度
        	String isChange = request.getParameter("isChange");//是否年度调整
        	map.put("agencyid", agencyid);
        	//List retList = this.budgetEditService.queryProjInputList(map,"BudgetEdit","query");
        	/*
        	//L:5.12修改
    		String cols="";//要查的字段
    		HashMap colInfos=getColInfo("TB_BUSI_BUDGETPROMAIN");//得到数据库表字段类型以便转换输出 clob date
    		Set set=colInfos.entrySet(); 
    	    Iterator it=set.iterator(); 
    	    while(it.hasNext()){ 
    	         Map.Entry me=(Map.Entry)it.next(); 
    	         if(me.getValue().equals("DATE")){
    	        	 cols+="to_char("+me.getKey()+",'yyyy-mm-dd') as "+me.getKey()+",";
    	         }else if(me.getValue().equals("CLOB")){
    	        	 cols+="to_char("+me.getKey()+") as "+me.getKey()+",";
    	         }else{
    	        	 cols+=me.getKey()+",";
    	         }
    	    } 
    	    cols=cols.substring(0, cols.length()-1);
    	    */
        	String sql="select BPGUID,AGUID,BPNAME,ISCHANGE from TB_BUSI_BUDGETPROMAIN where AGUID='"+agencyid+"' and petcode in (select petcode from tb_pubproexptype where ispro <> '1') "+
        			"  and prosta in (3,4,5,6,7) and ischange='"+isChange+"' and currentyear='"+year+"'  order by  PETCODE desc ";
        	Map querymap=new HashMap();
    		querymap.put("sql", sql);
        	List retList= this.dao.queryList(querymap, "pub_default_sql", "default_query_sql");
        	if(retList.size()==0){
        		return null;
        	}
        	this.responseInterface(retList);
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
        return null;
    }
    /*
     * 查找数据库中表的字段类型
     */
    public HashMap getColInfo(String tableName) throws OTSException{
		String sql="SELECT T.COLUMN_NAME,T.DATA_TYPE  FROM user_tab_columns T WHERE table_name = '"+tableName.toUpperCase()+"'";
		Map sqlmap = new HashMap();
		sqlmap.put("sql", sql);
		
		List<HashMap> tabs=dao.queryList(sqlmap, "pub_default_sql",this.dao.DEFAULT_QUERY_SQL_NAME);
		
		HashMap colInfos=new HashMap();
		for(HashMap tabitem:tabs){
			colInfos.put(tabitem.get("COLUMN_NAME").toString(), tabitem.get("DATA_TYPE").toString());
		}
		
		return colInfos;
	}
    /**
     * 查询项目类别表头
     * @return 项目类别
     * @throws Exception
     */
	public String showProjTypeTitle() throws Exception {
    	this.reportService.setTableName("TB_PUBPROEXPTYPE");
    	this.reportService.setType("treeList");
    	this.reportService.setColsDefProp("IsHide", "absHide");
    	this.reportService.getCol("PETCODE").setIsHide("false");
    	this.reportService.getCol("PETNAME").setIsHide("false");
    	this.reportService.setTblPara("dataURL", "ProjInput!showProjTypeData.do");
    	String projTypeXml = this.reportService.getReportTitle();
    	
    	this.responseInterface(projTypeXml);
    	return null;
    }
    /**
     * 查询项目类型数据
     * @return 项目类别
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public String showProjTypeData() throws Exception {
    	Map map = new HashMap();    	
    	
		List projTypeList = this.budgetEditService.queryProjInputList(map, "ProjManager", "queryProjType");
		this.responseInterface(projTypeList);
    	return null;
    }
    /**
     * 执行项目录入保存操作
     * @return null
     * @throws Exception
     */
    @SuppressWarnings({ "unused", "unchecked" })
    public String saveTabData() throws Exception{
    	CaUserVO CUV=(CaUserVO)session.get("session_user");
    	String usercode=CUV.getUsercode();
    	String ip = request.getRemoteAddr();
    	
		String mainData = request.getParameter("mainTab");
    	String detailData = request.getParameter("detailTab");
    	String listData = request.getParameter("dataList");
    	String aguid = request.getParameter("aguid");
    	String bpguid = request.getParameter("bpguid");
    	String currentyear = request.getParameter("year");//年度
    	String isChange =request.getParameter("isChanges");
    	
//测试添加数据到测试表
//    	String sql = "insert into t_test (CREATEDATE,mianStr,detailStr,listStr,usercode ,BUSITYPE) " +
//    			"values ( to_char(sysdate,'YYYY/MM/DD HH24:MI:SS'),'"+mainData+"','"+detailData+"'," +
//    					"'"+listData+"','"+CUV.getUsercode()+"','预算编报')";
//    	
//    	Map sqlMap = new HashMap();
//		sqlMap.put("sql", sql);
//		this.dao.excuteSql(sqlMap, "pub_default_sql", "default_create_sql");
    	
    	Map paramMap = new HashMap();
    	paramMap.put("aguid", aguid);
    	paramMap.put("inpuser", usercode);
    	paramMap.put("inpaguid",CUV.getOrgcode());
    	paramMap.put("cuv", CUV);
    	paramMap.put("ip", ip);
    	paramMap.put("currentyear", currentyear);
    	//如果前台传递过来bpguid则使用bpguid
    	if(!"".equals(bpguid)){
    		paramMap.put("bpguid", bpguid);
    	}   
    	
    	String   ischange=isChange.equals("")?"1":isChange;
    	
    	paramMap.put("ischange", ischange);
    	//处理主表信息
    	Map retMainMap = new HashMap();
    	if(!"".equals(mainData)){
    		retMainMap = this.reportService.retXMLForm(mainData);//.retXmlMap("", mainData);
    	}
    	//处理明细表信息
		Map retDetailMap = new HashMap();//= this.reportService.retXMLList(detailData);//.retXmlMap("", detailData);
		if(!"".equals(detailData)){
			retDetailMap = this.reportService.retXMLList(detailData);
		}
		
    	//处理挂接表的信息
		List list = new ArrayList();
    	String[] pubString = listData.split(",");
    	
    	for(int i=0;i<pubString.length;i++){
    		String pubData  =  pubString[i];
    		String[] tableString = pubData.split("==");
    		String tablename = tableString[0];
    		String tableData="";
    		if(tableString.length>1){
    			tableData = tableString[1];
    			Map pubMap = new HashMap();
    			pubMap.put(tablename,  this.reportService.retXMLList(tableData));
    			list.add(pubMap);
    		}  
    	}
				
		
		String orgtype=CUV.getOrgtype().toString();
		String orgcode = CUV.getOrgcode();
    	String userleven="";
		if(orgtype.equals("0")){
    		String otsql="select mofdeptype from t_pubmofdep where code ='"+orgcode+"'";
    		Map otmap=new HashMap();
    		otmap.put("sql", otsql);
    		Object otobj=this.dao.queryObject(otmap, "pub_default_sql", "query_guid");
    		String mofdeptype=otobj.toString();
    		if(mofdeptype.equals("0")||mofdeptype.equals("1")){
    			userleven="'0','-1','-2','-3'";
    		}else{
    			userleven="'0'";
    		}
    	}else if(orgtype.equals("1")){
    		String otsql="select levelno from t_pubagency where code ='"+orgcode+"'";
    		Map otmap=new HashMap();
    		otmap.put("sql", otsql);
    		Object otobj=this.dao.queryObject(otmap, "pub_default_sql", "query_guid");
    		String levelno="'"+otobj.toString()+"'";
    		userleven=levelno;
    	}
		paramMap.put("userleven", userleven);
		paramMap.put("checkdefsta", "0");
		int result = this.budgetEditService.doInsertProjDatas(retMainMap,retDetailMap,list,paramMap);
		//basicDataEntryService.updateFormulaRefresh("",aguid,userleven);
		
		String cols="";//要查的字段
		HashMap colInfos=getColInfo("TB_BUSI_BUDGETPROMAIN");//得到数据库表字段类型以便转换输出 clob date
		Set set=colInfos.entrySet(); 
	    Iterator its=set.iterator(); 
	    while(its.hasNext()){ 
	         Map.Entry me=(Map.Entry)its.next(); 
	         if(me.getValue().equals("DATE")){
	        	 cols+="to_char("+me.getKey()+",'yyyy-mm-dd') as "+me.getKey()+",";
	         }else if(me.getValue().equals("CLOB")){
	        	 cols+="to_char("+me.getKey()+") as "+me.getKey()+",";
	         }else{
	        	 cols+=me.getKey()+",";
	         }
	    } 
	    cols=cols.substring(0, cols.length()-1);
		
		
		Object insertMainMap = retMainMap.get("newRow");
		if(null!=insertMainMap){
			List retlist = (List)insertMainMap;
			Map newMap = (Map)retlist.get(0);
			Iterator it = newMap.entrySet().iterator();
			String condition = "";
			while (it.hasNext()) {
				Entry e = (Entry) it.next();
				// 获取HASHMAP中的每个KEY
				Object keyObj = e.getKey();
				Object valueObj = e.getValue();
				if(null != keyObj && null != valueObj&&!"".equals(valueObj.toString())){
					condition = ("".equals(condition)?"":(condition+" and ")) + keyObj.toString()+"='"+valueObj.toString()+"'";
					
				}
				
			}
			StringBuilder sb = new StringBuilder();
			sb.append("select "+cols+" from TB_BUSI_BUDGETPROMAIN ");
			sb.append(" where ");
			sb.append(" bpguid='");
			sb.append(paramMap.get("bpguid").toString());
			sb.append("'");
			Map retMap = new HashMap();
			retMap.put("sql", sb.toString());
			List lst = this.budgetEditService.queryProjInputList(retMap, "pub_default_sql", "default_query_sql");
			this.responseInterface(lst.size()>0?(Map)lst.get(0):"");
		}else{			
			StringBuilder sb = new StringBuilder();
			sb.append("select "+cols+" from TB_BUSI_BUDGETPROMAIN ");
			sb.append(" where bpguid='"+bpguid+"'");
			Map retMap = new HashMap();
			retMap.put("sql", sb.toString());
			List lst = this.projManagerService.queryProjInputList(retMap, "pub_default_sql", "default_query_sql");
			this.responseInterface(lst.size()>0?(Map)lst.get(0):"");
		}
		
    	return null;
    }
    
    
    /**
     * 查询项目主信息表的FREEFORM
     * @return null
     * @throws Exception
     */
    public void showBudgetMainForm() throws Exception {
    	CaUserVO cuv=(CaUserVO)session.get("session_user");
    	
    	String projId = request.getParameter("ids");    	
    	String tablename  = "TB_BUSI_BUDGETPROMAIN";
    	String tableid = budgetEditService.getTableId(tablename);    	
    	String petcode  = request.getParameter("petcode");
    	String aguid = request.getParameter("aguid");
    	String acode = request.getParameter("acode");
    	String edit = request.getParameter("edit")==null?"true":request.getParameter("edit");
    	String currentyear=request.getParameter("currentyear");
    	String retXml=budgetEditService.showBudgetMainTitle(currentyear,tableid,tablename,cuv,petcode,projId,aguid,acode,edit);
    	request.setAttribute("petcode", petcode);
    	this.responseInterface(retXml);
    }
    /**
     * 查询主表数据
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public void showBudgetMainFormdata() throws Exception{
    	String bpguid = request.getParameter("ids");
		String cols="";//要查的字段
		HashMap colInfos=getColInfo("TB_BUSI_BUDGETPROMAIN");//得到数据库表字段类型以便转换输出 clob date
		Set set=colInfos.entrySet(); 
	    Iterator it=set.iterator(); 
	    while(it.hasNext()){ 
	         Map.Entry me=(Map.Entry)it.next(); 
	         if(me.getValue().equals("DATE")){
	        	 cols+="to_char("+me.getKey()+",'yyyy-mm-dd') as "+me.getKey()+",";
	         }else if(me.getValue().equals("CLOB")){
	        	 cols+="to_char("+me.getKey()+") as "+me.getKey()+",";
	         }else{
	        	 cols+=me.getKey()+",";
	         }
	    } 
	    cols=cols.substring(0, cols.length()-1);
    	String sql = "select "+cols+" from tb_busi_budgetpromain t where t.bpguid='"+bpguid+"'";
    	Map map = new HashMap();
    	map.put("sql", sql);
    	List list = budgetEditService.queryProjInputList(map, "pub_default_sql", "default_query_sql");
    	map = (Map)list.get(0);
    	this.responseInterface((Map)list.get(0));
    }
    
    /**
     * 导出主表信息 Excel 文件
     * @author lwj
     */
    public void exportBudgetMain() throws Exception {
    	try {
    	String bpguid = request.getParameter("ids");
    	String yxxm = request.getParameter("yxxm");
    	String petcode = request.getParameter("petcode");
    	String xbbsjl = request.getParameter("xbbsjl");
    	String epcode = request.getParameter("epcode");
    	String prosta = request.getParameter("prosta");
    	String xmxz = request.getParameter("xmxz");
    	String xmjc = request.getParameter("xmjc");
    	String mdcode = request.getParameter("mdcode");
		String cols="";//要查的字段
		HashMap colInfos=getColInfo("TB_BUSI_BUDGETPROMAIN");//得到数据库表字段类型以便转换输出 clob date
		Set set=colInfos.entrySet(); 
	    Iterator it=set.iterator(); 
	    while(it.hasNext()){
	    	
	        Map.Entry me=(Map.Entry)it.next(); 
	         if(me.getValue().equals("DATE")){
	        	 cols+="to_char("+me.getKey()+",'yyyy-mm-dd') as "+me.getKey()+",";
	         }else if(me.getValue().equals("CLOB")){
	        	 cols+="to_char("+me.getKey()+") as "+me.getKey()+",";
	         }else{
	        	 cols+=me.getKey()+",";
	         }
	    } 
	    cols=cols.substring(0, cols.length()-1);
	    String sql = "select "+cols+" from tb_busi_budgetpromain t where t.bpguid='"+bpguid+"'";
    	Map map = new HashMap();
    	map.put("sql", sql);
    	// 查询导出信息
    	BudgetpromainVO budget = (BudgetpromainVO)budgetEditService.queryBudgetMain(map, "pub_default_sql", "queryBudgetMain");
    	budget.setYXXM(yxxm);
		budget.setPETCODE(petcode);
		budget.setXBBSJL(xbbsjl);
		budget.setEpcode(epcode);
		budget.setPROSTA(prosta);
		budget.setXMXZ(xmxz);
		budget.setXMJC(xmjc);
		budget.setMDCODE(mdcode);

    	// 导出设置
    	HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("sheet1");
		wb.setSheetName(0, "主表信息");
		HSSFHeader header = sheet.getHeader();
		header.setCenter("主表信息");
		// 设置表头
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell((short) 0);
		cell.setCellValue("项目唯一码");
		cell = row.createCell((short) 1);
		cell.setCellValue("项目类别");
		cell = row.createCell((short) 2);
		cell.setCellValue("项目属性");
		cell = row.createCell((short) 3);
		cell.setCellValue("项目名称");
		cell = row.createCell((short) 4);
		cell.setCellValue("延续项目");
		cell = row.createCell((short) 5);
		cell.setCellValue("功能科目");
		cell = row.createCell((short) 6);
		cell.setCellValue("项目状态");
		cell = row.createCell((short) 7);
		cell.setCellValue("项目性质");
		cell = row.createCell((short) 8);
		cell.setCellValue("项目级次");
		cell = row.createCell((short) 9);
		cell.setCellValue("预算项目库编号");
		cell = row.createCell((short) 10);
		cell.setCellValue("发改委项目编码");
		cell = row.createCell((short) 11);
		cell.setCellValue("财政备选项目库编号");
		cell = row.createCell((short) 12);
		cell.setCellValue("纳入财政安排编号");
		cell = row.createCell((short) 13);
		cell.setCellValue("本年单位一般公共预算支出小计");
		cell = row.createCell((short) 14);
		cell.setCellValue("本年单位政府性基金预算支出小计");
		cell = row.createCell((short) 15);
		cell.setCellValue("上年单位一般公共预算支出小计");
		cell = row.createCell((short) 16);
		cell.setCellValue("上年单位政府性基金预算支出小计");
		cell = row.createCell((short) 17);
		cell.setCellValue("序号");
		cell = row.createCell((short) 18);
		cell.setCellValue("项目实施单位(科室)");
		cell = row.createCell((short) 19);
		cell.setCellValue("归口科室");
		cell = row.createCell((short) 20);
		cell.setCellValue("项目起始时间");
		cell = row.createCell((short) 21);
		cell.setCellValue("项目终止时间");
		cell = row.createCell((short) 22);
		cell.setCellValue("项目负责人");
		cell = row.createCell((short) 23);
		cell.setCellValue("联系电话 (手机号)");
		cell = row.createCell((short) 24);
		cell.setCellValue("序号");
		cell = row.createCell((short) 25);
		cell.setCellValue("备注");
		cell = row.createCell((short) 26);
		cell.setCellValue("项目单位主要职责");
		cell = row.createCell((short) 27);
		cell.setCellValue("项目概况");
		cell = row.createCell((short) 28);
		cell.setCellValue("项目立项依据");
		cell = row.createCell((short) 29);
		cell.setCellValue("项目立项必要性");
		cell = row.createCell((short) 30);
		cell.setCellValue("项目立项可行性");
		cell = row.createCell((short) 31);
		cell.setCellValue("项目资金测算过程");
		cell = row.createCell((short) 32);
		cell.setCellValue("项目总体绩效目标");
		cell = row.createCell((short) 33);
		cell.setCellValue("年度绩效指标明细 (产出指标)");
		cell = row.createCell((short) 34);
		cell.setCellValue("年度绩效指标明细 (效益指标)");
		cell = row.createCell((short) 35);
		cell.setCellValue("年度绩效指标明细 (服务对象满意度指标)");
		cell = row.createCell((short) 36);
		cell.setCellValue("调整原因");
		cell = row.createCell((short) 37);
		cell.setCellValue("其他需要说明事项");
		//  设置数据行
		row = sheet.createRow(1);
		cell = row.createCell((short) 0);
		cell.setCellValue(budget.getBpguid());// 项目编码
		cell = row.createCell((short) 1);
		cell.setCellValue(budget.getPETCODE());// 项目类别
		cell = row.createCell((short) 2);
		cell.setCellValue(budget.getXBBSJL());// 项目属性
		cell = row.createCell((short) 3);
		cell.setCellValue(budget.getBPNAME());// 项目名称
		cell = row.createCell((short) 4);
		cell.setCellValue(budget.getYXXM());// 延续项目
		cell = row.createCell((short) 5);
		cell.setCellValue(budget.getEpcode());// 功能科目
		cell = row.createCell((short) 6);
		cell.setCellValue(budget.getPROSTA());// 项目状态
		cell = row.createCell((short) 7);
		cell.setCellValue(budget.getXMXZ());// 项目性质
		cell = row.createCell((short) 8);
		cell.setCellValue(budget.getXMJC());// 项目级次
		cell = row.createCell((short) 9);
		cell.setCellValue(budget.getBMXMKBH());// 预算项目库编号
		cell = row.createCell((short) 10);
		cell.setCellValue(budget.getFGWXMBM());// 发改委项目编码
		cell = row.createCell((short) 11);
		cell.setCellValue(budget.getCZBXXMKBH());// 财政备选项目库编号
		cell = row.createCell((short) 12);
		cell.setCellValue(budget.getNRCZAPBH());// 纳入财政安排编号
		cell = row.createCell((short) 13);
		cell.setCellValue(budget.getDYJDZC());// 本年单位一般公共预算支出小计
		cell = row.createCell((short) 14);
		cell.setCellValue(budget.getDEJDZC());// 本年单位政府性基金预算支出小计
		cell = row.createCell((short) 15);
		cell.setCellValue(budget.getDSJDZC());// 上年单位一般公共预算支出小计
		cell = row.createCell((short) 16);
		cell.setCellValue(budget.getDSIJDZC());// 上年单位政府性基金预算支出小计
		cell = row.createCell((short) 17);
		String deptorder = (null == budget.getDEPTORDER()? "" : budget.getDEPTORDER().toString());
		cell.setCellValue(deptorder);// 序号
		cell = row.createCell((short) 18);
		cell.setCellValue(budget.getXMSSDW());// 项目实施单位(科室)
		cell = row.createCell((short) 19);
		cell.setCellValue(budget.getMDCODE());// 归口科室
		cell = row.createCell((short) 20);
		cell.setCellValue(budget.getBDATE());// 项目起始时间
		cell = row.createCell((short) 21);
		cell.setCellValue(budget.getEDATE());// 项目终止时间
		cell = row.createCell((short) 22);
		cell.setCellValue(budget.getXMFZR());// 项目负责人
		cell = row.createCell((short) 23);
		cell.setCellValue(budget.getLXDH());//联系电话 (手机号)
		cell = row.createCell((short) 24);
		cell.setCellValue(budget.getXH());// 序号
		cell = row.createCell((short) 25);
		cell.setCellValue(budget.getXMJJ());// 备注
		cell = row.createCell((short) 26);
		cell.setCellValue(budget.getSLZB());// 项目单位主要职责
		cell = row.createCell((short) 27);
		cell.setCellValue(budget.getZLZB());// 项目概况
		cell = row.createCell((short) 28);
		cell.setCellValue(budget.getSXZB());// 项目立项依据
		cell = row.createCell((short) 29);
		cell.setCellValue(budget.getCBZB());// 项目立项必要性
		cell = row.createCell((short) 30);
		cell.setCellValue(budget.getJJXYZB());// 项目立项可行性
		cell = row.createCell((short) 31);
		cell.setCellValue(budget.getSHXYZB());// 项目资金测算过程
		cell = row.createCell((short) 32);
		cell.setCellValue(budget.getHJXYZB());// 项目总体绩效目标
		cell = row.createCell((short) 33);
		cell.setCellValue(budget.getKCXYXZB());// 年度绩效指标明细 (产出指标)
		cell = row.createCell((short) 34);
		cell.setCellValue(budget.getFWDXMYDZB());// 年度绩效指标明细 (效益指标)
		cell = row.createCell((short) 35);
		cell.setCellValue(budget.getNDJXZBMX());// 年度绩效指标明细 (服务对象满意度指标)
		cell = row.createCell((short) 36);
		cell.setCellValue(budget.getDZYY());// 调整原因
		cell = row.createCell((short) 37);
		cell.setCellValue(budget.getQTXYSMSX());// 其他需要说明事项
		sheet.setColumnWidth((short)0,(short)10000);
		sheet.setColumnWidth((short)1,(short)10000);
		sheet.setColumnWidth((short)2,(short)10000);
		sheet.setColumnWidth((short)3,(short)10000);
		sheet.setColumnWidth((short)5,(short)10000);
		sheet.setColumnWidth((short)4,(short)10000);
		sheet.setColumnWidth((short)6,(short)10000);
		sheet.setColumnWidth((short)7,(short)10000);
		sheet.setColumnWidth((short)8,(short)10000);
		sheet.setColumnWidth((short)9,(short)10000);
		sheet.setColumnWidth((short)10,(short)10000);
		sheet.setColumnWidth((short)11,(short)10000);
		sheet.setColumnWidth((short)12,(short)10000);
		sheet.setColumnWidth((short)13,(short)10000);
		sheet.setColumnWidth((short)14,(short)10000);
		sheet.setColumnWidth((short)15,(short)10000);
		sheet.setColumnWidth((short)16,(short)10000);
		sheet.setColumnWidth((short)17,(short)10000);
		sheet.setColumnWidth((short)18,(short)10000);
		sheet.setColumnWidth((short)19,(short)10000);
		sheet.setColumnWidth((short)20,(short)10000);
		sheet.setColumnWidth((short)21,(short)10000);
		sheet.setColumnWidth((short)22,(short)10000);
		sheet.setColumnWidth((short)23,(short)10000);
		sheet.setColumnWidth((short)24,(short)10000);
		sheet.setColumnWidth((short)25,(short)10000);
		sheet.setColumnWidth((short)26,(short)10000);
		sheet.setColumnWidth((short)27,(short)10000);
		sheet.setColumnWidth((short)28,(short)10000);
		sheet.setColumnWidth((short)29,(short)10000);
		sheet.setColumnWidth((short)30,(short)10000);
		sheet.setColumnWidth((short)31,(short)10000);
		sheet.setColumnWidth((short)32,(short)10000);
		sheet.setColumnWidth((short)33,(short)10000);
		sheet.setColumnWidth((short)34,(short)10000);
		sheet.setColumnWidth((short)35,(short)10000);
		sheet.setColumnWidth((short)36,(short)10000);
		sheet.setColumnWidth((short)37,(short)10000);
    	
		String nameString = java.net.URLEncoder.encode("主表信息.xls", "UTF-8");
		response.reset(); //请出首部的空白行
		response.setContentType("application/msexcel;charset=gbk"); //用于设置输出的文档MIME类型，默认为text/html
		response.addHeader("Content-disposition", "attachment;filename="+nameString); 
		OutputStream outputStream = getResponse().getOutputStream();
		wb.write(outputStream);
		outputStream.flush();
		outputStream.close();
		
    	} catch (Exception e) {
			e.printStackTrace();
		}
		
    }
    
    /**
     * 根据选中的类别来加载tab页面
     * @return json
     * @throws OTSException
     * @throws IOException 
     */
    @SuppressWarnings("unchecked")
	public String queryTabpanelByPet() throws OTSException, IOException{
    	String petcode = request.getParameter("petcode");
    	Map map  = new HashMap();
    	map.put("PETCODE", petcode);
    	List list = this.budgetEditService.queryProjInputList(map, "ProjManager", "queryProjTabByType");
        	
    	this.responseInterface(list);
    	return null;
    }
    
    
    public String showAttchment(){
    	return "attchment";
    }

    /**
     * 创建只有freeformbar的treelist
     * @throws DocumentException 
     * @throws NoSuchMethodException 
     * @throws ClassNotFoundException 
     * @throws OTSException 
     * @throws SecurityException 
     * @throws IOException 
     */
    public String showButtonTreeList() throws SecurityException, OTSException, ClassNotFoundException, NoSuchMethodException, DocumentException, IOException{
    	this.reportService.setTableName("");
       	this.reportService.setColsDefProp("width", "100");
    	this.reportService.setType("treeListAndBar");
    	this.reportService.AddFFBtn("btn1", "增加");
    	this.reportService.AddFFBtn("btn2", "保存");
    	this.reportService.setFFBtn("btn1");
    	this.reportService.setFFBtn("btn2");
    	
    	String retXml = reportService.getReportTitle();
    	
     	super.responseInterface(retXml);
    	return null;
    }
    //删除项目操作
    @SuppressWarnings("unchecked")
	public String delTabData() throws OTSException, IOException{
    	CaUserVO CUV=(CaUserVO)session.get("session_user");
    	String ip = request.getRemoteAddr();
    	String aguid = request.getParameter("aguid");
    	String bpguid = request.getParameter("bpguid");
    	String petcode = request.getParameter("petcode");
    	//如果项目为项目类的则不允许删除，需要退回为备选状态进行删除
    	if(petcode!=null&&!"".equals(petcode)&&!"null".equals(petcode)){
    		String sql = "select * from tb_pubproexptype t where t.petcode='"+petcode+"'";
    		Map map  = new HashMap();
    		map.put("sql", sql);
    		List list = this.budgetEditService.queryProjInputList(map, "pub_default_sql", "default_query_sql");
    		Map retMap = (Map)list.get(0);
    		String ispro = retMap.get("ISPRO").toString();
    		if("1".equals(ispro)){
    			retMap.clear();
    			retMap.put("message", "没有权限删除项目类项目!");
    			this.responseInterface(retMap);
    			return null;
    		}
    		
    	}
    	String orgtype=CUV.getOrgtype().toString();
		String orgcode = CUV.getOrgcode();
    	String userleven="";
		if(orgtype.equals("0")){
    		String otsql="select mofdeptype from t_pubmofdep where code ='"+orgcode+"'";
    		Map otmap=new HashMap();
    		otmap.put("sql", otsql);
    		Object otobj=this.dao.queryObject(otmap, "pub_default_sql", "query_guid");
    		String mofdeptype=otobj.toString();
    		if(mofdeptype.equals("0")||mofdeptype.equals("1")){
    			userleven="'0','-1','-2','-3'";
    		}else{
    			userleven="'0'";
    		}
    	}else if(orgtype.equals("1")){
    		String otsql="select levelno from t_pubagency where code ='"+orgcode+"'";
    		Map otmap=new HashMap();
    		otmap.put("sql", otsql);
    		Object otobj=this.dao.queryObject(otmap, "pub_default_sql", "query_guid");
    		String levelno="'"+otobj.toString()+"'";
    		userleven=levelno;
    	}

		basicDataEntryService.updateFormulaRefresh("",aguid,userleven);
    	this.budgetEditService.delTabData(bpguid,petcode,CUV,ip);
    	return null;
    }

    
    
    /**
	 * 根据传递进来的参数执行通用SQL语句查询所有数据
	 * 
	 * @param map 暂时必要字段为TABLENAME即表明
	 * @return 数据列表
	 * @throws OTSException
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	public int createTable(Map map) throws OTSException {
		int a = 0;
		String physicalname = map.get("physicalname").toString();
		String max = map.get("max").toString();
		String max1 = String.valueOf((Integer.parseInt(max) + 1));
		String tableid = map.get("tableid").toString();
		//String sql = "create table " + physicalname + "(guid       VARCHAR2(38) default SYS_GUID(),agencyguid       VARCHAR2(38));\n comment on column " + physicalname + ".guid is \'唯一标识\';\n comment on column " + physicalname + ".agencyguid  is \'单位编码标识\';";
		String sql = "create table " + physicalname + "(guid       VARCHAR2(38) default SYS_GUID(),agencyguid       VARCHAR2(38))";
		String sqlinsert = "insert into tb_systablecols(colid, tableid, columnid, colname, physicalname, coldesc, coltype, iskey, cannull) values(" + max + ",\'" + tableid + "\',\'001\',\'唯一标识ID\',\'guid\',\'唯一标识ID\',\'003\',\'1\',\'0\')";
		String sqlinsert1 = "insert into tb_systablecols(colid, tableid, columnid, colname, physicalname, coldesc, coltype, iskey, cannull) values(" + max1 + ",\'" + tableid + "\',\'002\',\'部门编号\',\'agencyguid\',\'部门编号\',\'003\',\'0\',\'0\')";
		Map sqlmap = new HashMap();
		sqlmap.put("sql", sql);
		if(physicalname != ""){
			a =  dao.excuteSql(sqlmap, "pub_default_sql",this.dao.DEFAULT_CREATE_SQL_NAME);
		}
		sqlmap = new HashMap();
		sqlmap.put("sql", sqlinsert);
		
		a =  dao.excuteSql(sqlmap, "pub_default_sql",this.dao.DEFAULT_CREATE_SQL_NAME);
		sqlmap = new HashMap();
		sqlmap.put("sql", sqlinsert1);
		
		a =  dao.excuteSql(sqlmap, "pub_default_sql",this.dao.DEFAULT_CREATE_SQL_NAME);
		
		return a;
	}
	
	 /**
	 * 根据传递进来的参数执行通用SQL语句查询所有数据
	 * 
	 * @param map 暂时必要字段为TABLENAME即表明
	 * @return 数据列表
	 * @throws OTSException
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	public int dropTable(Map map) throws OTSException {
		int a = 0;
		String physicalname = map.get("physicalname").toString();
		String tableid = map.get("key").toString();
		//String sql = "create table " + physicalname + "(guid       VARCHAR2(38) default SYS_GUID(),agencyguid       VARCHAR2(38));\n comment on column " + physicalname + ".guid is \'唯一标识\';\n comment on column " + physicalname + ".agencyguid  is \'单位编码标识\';";
		String sql = "drop table " + physicalname ;
		String sql1 = "delete from tb_systablecols where tableid=" + tableid ;
		
		Map sqlmap = new HashMap();
		sqlmap.put("sql", sql);
		if(physicalname != ""){
			a =  dao.excuteSql(sqlmap, "pub_default_sql",this.dao.DEFAULT_CREATE_SQL_NAME);
		}
		sqlmap = new HashMap();
		sqlmap.put("sql", sql1);
		a =  dao.excuteSql(sqlmap, "pub_default_sql",this.dao.DEFAULT_CREATE_SQL_NAME);

		return a;
	}
	
	/**
	 * 根据传递进来的参数执行通用SQL语句查询所有数据
	 * 
	 * @param map 暂时必要字段为TABLENAME即表明
	 * @return 数据列表
	 * @throws OTSException
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	public List getTableName(Map map) throws OTSException {
		String sql = "";
		if(null != map.get("key")){
			String key = map.get("key").toString();
		    sql = "select * from  TB_SYSTABLES where tableid = \'" + key + "\'";
		}
		
		if(null != map.get("max")){
			sql = "select max(colid)+1 as max from tb_systablecols";
		}
		Map sqlmap = new HashMap();
		sqlmap.put("sql", sql);
		
		return dao.queryList(sqlmap, "pub_default_sql",this.dao.DEFAULT_QUERY_SQL_NAME);
	}
	
	
    /**
     * 查询表数据
     * @return JSON
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public String showNewData() throws Exception {
    	String tablename=request.getParameter("tablename");
    	String xmlReport=request.getParameter("xmlReport");
    	String physicalname = "";
    	String key = "";
    	
    	Map xmlMap = XmlUtil.getXml(xmlReport, new Object());
    	
    	if(null != xmlMap.get("newRow")){
    		Map valueMap = (Map)((Map)xmlMap.get("newRow")).get("row");
    		physicalname = valueMap.get("physicalname").toString();
    		String tableid = valueMap.get("tableid").toString();
    		int max = 0;
    		Map maxMap = new HashMap();
    		maxMap.put("max", max);
    		List list = this.getTableName(maxMap);
    		for(int i=0,len=list.size();i<len;i++){
				Map map = (HashMap)list.get(i);
				
				if(null != map.get("MAX")){
					Map createmap = new HashMap();
		        	createmap.put("physicalname", physicalname);
		        	createmap.put("max", map.get("MAX").toString());
		        	createmap.put("tableid", tableid);
		    		@SuppressWarnings("unused")
					int intReturn = this.createTable(createmap);
				}
			}
    		
    	}
    	
    	if(null != xmlMap.get("deletedRow")){
    		Map valueMap = new HashMap();
    		if(null!=((Map)xmlMap.get("deletedRow"))){
				valueMap = (Map)((Map)xmlMap.get("deletedRow"));
			}
    	
    		key = valueMap.get("key").toString();
    		Map getNamemap = new HashMap();
    		getNamemap.put("key", key);
    		List list = this.getTableName(getNamemap);  
    			for(int i=0,len=list.size();i<len;i++){
    				Map map = (HashMap)list.get(i);
    				
    				if(map.get("PHYSICALNAME") != null){
    					Map dropmap = new HashMap();
    					dropmap.put("physicalname", map.get("PHYSICALNAME").toString());
    					dropmap.put("key", key);
    					this.dropTable(dropmap);
    				}
    			}
    		
    	}
    	
    	reportService.analysisAndSaveXmlReport(tablename,xmlReport);
    	
    	Map map = new HashMap();
    	map.put("tablename", tablename);
    	/*if(null != xmlMap.get("newRow")){
    		int intReturn = this.createTable(map);
    	}*/
    	List list = reportService.getPhysicTableDatas(map); 
    	
    	/*for(int i=0; i<list.size(); i++){
    		System.out.print(list.get(i)+ "------");
    	}*/
    	super.responseInterface(list);
        return null;
    }    
    /**
     * 查询表数据
     * @return XML
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public String showTreeData() throws Exception {
    	Map map = new HashMap();
    	map.put("tablename", tablename);
    	List list = reportService.getPhysicTableDatas(map);    
    	for(int i=0; i<list.size(); i++){
    		System.out.print(list.get(i)+ "------");
    	}
    	
    	Map newMap = new HashMap();
		newMap.put("list", list);
		String treeXml = this.tb_puboptionsXml(newMap);
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.print(treeXml);
		out.flush();
		out.close();
        return null;
    	
    }   
 /*   
    //拼装treexml
    @SuppressWarnings("unchecked")
	public String treexml() throws Exception{
    	Map map = new HashMap();
    	map.put("tablename", tablename);
    	String retXml = reportService.treexml(map);
    	
    	response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.print(retXml);
		out.flush();
		out.close();
        return null;
    }*/
  //拼項目類別定義表
    @SuppressWarnings("unchecked")
	public  String tb_puboptionsXml(Map objListMap) {
		// 使用 DocumentHelper 类创建一个文档实例
		Document document = DocumentHelper.createDocument();
		// 使用 addElement() 方法创建根元素 TreeList
		Element dataElement = document.addElement("Data");

		Element itemsElement = dataElement.addElement("items");
		List list = (List)objListMap.get("list");
		Element itemElement1 = itemsElement.addElement("item");
		itemElement1.addAttribute("ID","000");
		itemElement1.addAttribute("str","报表业务类型");
		itemElement1.addAttribute("code","000");
   
		for(int i=0,len=list.size();i<len;i++){
			Map map = (HashMap)list.get(i);
 			if(map.get("OTYPE").toString().equals("004")){
 				//在 TreeList 元素中使用 addElement() 方法增加 Fonts 元素
 				Element itemElement = itemsElement.addElement("item");
 				//TB_PUBPROREVTYPEVO agency = (TB_PUBPROREVTYPEVO)list.get(i);
 				
				itemElement.addAttribute("ID",map.get("OTVALUE").toString());
				itemElement.addAttribute("str",map.get("OTVNAME").toString());
				//itemElement.addAttribute("code",map.get("PRTCODE").toString());
				//itemElement.addAttribute("PID",map.get("PRENTID").toString());
				itemElement.addAttribute("PID","000");
 			}	
		}
		return document.asXML();
	}
    
    
    //拼項目類別定義表
    @SuppressWarnings("unchecked")
	public  String treedataXml(Map objListMap) {
		// 使用 DocumentHelper 类创建一个文档实例
		Document document = DocumentHelper.createDocument();
		// 使用 addElement() 方法创建根元素 TreeList
		Element dataElement = document.addElement("Data");

		Element itemsElement = dataElement.addElement("items");
		List list = (List)objListMap.get("list");
		Element itemElement1 = itemsElement.addElement("item");
		itemElement1.addAttribute("ID","0");
		itemElement1.addAttribute("str","项目收入类别");
		itemElement1.addAttribute("code","000");
   
		for(int i=0,len=list.size();i<len;i++){
			Map map = (HashMap)list.get(i);
			//在 TreeList 元素中使用 addElement() 方法增加 Fonts 元素
 			Element itemElement = itemsElement.addElement("item");
 			//TB_PUBPROREVTYPEVO agency = (TB_PUBPROREVTYPEVO)list.get(i);
		
			itemElement.addAttribute("ID",map.get("GUID").toString());
			itemElement.addAttribute("str",map.get("PRTNAME").toString());
			itemElement.addAttribute("code",map.get("PRTCODE").toString());
			itemElement.addAttribute("PID",map.get("PRENTID").toString());
		}
		return document.asXML();
	}

	public void getToolBar() throws IOException, OTSException { 
		super.responseInterface(budgetEditService.getToolBarXML());
	}
	/**
	 * 根据项目ID查询是否项目标识
	 * @throws OTSException 
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public void queryIsproByPetcode() throws OTSException, IOException{
		//String petcode = request.getParameter("petcode");
		String bpguid = request.getParameter("bpguid");
		String sql = "select ispro from tb_busi_budgetpromain t1 left join tb_pubproexptype t2 on t1.petcode = t2.petcode  where t1.bpguid='"+bpguid+"' and t1.prosta='3'";
		Map map = new HashMap();
		map.put("sql", sql);
		List list = this.budgetEditService.queryProjInputList(map, "pub_default_sql", "default_query_sql");
		map.clear();
		if(list.size()>0){
			map.put("ispro", "1");
		}else{
			map.put("ispro", "0");
		}
		this.responseInterface(map);
	}
	
	public void getMaxlsh() throws Exception {
		Map map =new HashMap();
    	String aguid = request.getParameter("aguid");
    	String year = request.getParameter("year");//年度
    	String sql="select XMSBLSH from TB_BUSI_BUDGETPROMAIN where AGUID='"+aguid+"' and currentyear='"+year+"'" +
		" and petcode not in (select petcode from tb_pubproexptype where ispro = '1') order by  XMSBLSH desc";
    	Map querymap=new HashMap();
		querymap.put("sql", sql);
    	List retList= this.dao.queryList(querymap, "pub_default_sql", "default_query_sql");
    	if(retList.size()==0){
    		map.put("XMSBLSH", "0");
    	}else{
    		map=(Map)retList.get(0);
    	}
    	this.responseInterface(map);
    }
	
	
	public static void main(String[] args) {
		
		String str = "1";
		
		String[] aa = str.split(",");
		System.out.println(aa.length);
		
		
		
	}
	
	
}
