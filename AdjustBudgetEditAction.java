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
 * ˵����Ԥ��౨ACTION
 * @version 1.0
 */
@SuppressWarnings("serial")
public class AdjustBudgetEditAction extends PagedAction  {
	/**
     * ������ֻ��Ҫ����ʹ�õ���ҵ���߼������,��ʵ����spring���𴴽�
     */
	//ǰ̨���ݵ�TABLENAME��̬�仯
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
	// ���ݷ��ʲ����
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
    // ��ʾ��ҳ
    public String showIndex() throws Exception {
    	String CURRENTYEAR = commonService.getParameter((List)session.get(SystemConst.SESSION_SYSPARA), "CURRENTYEAR");
		request.setAttribute("currentyear", CURRENTYEAR);
		Map<String,Object> map = (Map<String, Object>) request.getSession().getAttribute("BUDGETEND");
		request.setAttribute("budgetend", map.get("BUDGETEND"));
        return INDEX;
    } 

    // ��ʾͨ��report����
    public String showPubIndex() throws Exception {
        return "pubindex";
    }    
    // ��ʾ��ϸ�����
    public String showReport() throws Exception {
    	tablename = request.getParameter("tablename");
        return "detailreport";
    }
    //��������Ժ���ϸ����ת����
    public String showAddReport() throws Exception {
    	String CURRENTYEAR = commonService.getParameter((List)session.get(SystemConst.SESSION_SYSPARA), "CURRENTYEAR");
		request.setAttribute("currentyear", CURRENTYEAR);
    	request.setAttribute("add", true);
    	return "detailreport";
    }
    // ��ʾ��Ŀ������ҳ
    public String showAddIndex() throws Exception {
    	/**
    	 * TB_BUSI_BUDGETPROMAIN  ��ǰ̨������λid,��Ŀid������̨��ѯ����Ŀ��𣬹��ܿ�Ŀ 
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
    		//��ѯ��Ŀ�걨�����Ϣ
    		//ͨ����Ŀ��ѯ��Ŀ���
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
     * ������Ŀ����ѯ���÷���
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
     * ������Ŀ����ѯ���ܿ�Ŀ
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
     * ��ѯ��Ŀ���
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
     * ���ݵ�λ��ѯ��ڴ���
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
    // ������Ŀ¼��
    public String doInsert() throws Exception {
        return NONE;
    }
    /**
     * ��ѯ��Ŀ�����ı���Ϣ(���TAB)
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public String queryTabpanel() throws Exception{
    	//��ѯ���ñ��Ƿ�ɼ�
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
     * ���������ݼ����ڱ��������е�λ
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
     * @Description: У��������λ������
     * @param sendaguid ��Ҫ����У��ĵ�λ��
     * @param busitype ҵ�����ͣ���Ҫ�õ���ģ�鴫�ò�������У��ȫ������0,1,2,3,4
     * @param change �Ƿ����У���־��һ��ģ�鶼����У�鵥λȫ��������д0 ��
     * @param identity ��½�û���ݣ�����ɸѡУ�鹫ʽ�ġ�0 ������1 ���ţ�2�ǵ׼���λ��3�׼���λ
     * @return List   �������� 
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
     * ��ѯ���ʽ�������ݼ���
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
     * ��Ŀ��ϸ���ͷ������
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
     * �����ʽ�޸�����  Ȼ�� ��ѯ���������ҳ��
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
						retMap.put("message", "ˢ�¡�" + forMulaRefExeService.guidtoname(map.get("aguid").toString()) + "���ġ�" +  forMulaRefExeService.tableidtoname(map.get("tableid").toString()) +  "����"+map.get("formulatype").toString()+"��" + map.get("colname") + "����ʱ ʧ��");
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
     * Load ����ǰ�ж� ���ʽ�ֶε�ֵ ����ʽsql��ѯ�Ľ���Ƿ���ȣ��������˵�� ���ʽԴ�����ݷ����ı�
     * ��ʾ�û��Ƿ�ˢ�����ݱ�֤����ͬ�� 
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
    	//����Ŀ��ı�ȫ��ȡ��
    	String sql = "select * from tb_systables t where (t.busitype='0' or t.isgovpch='1' or t.tableid='001') and t.isused='1' and t.sysflag in ('0','1')";
    	map.put("sql",sql);
    	List projTableList = this.projManagerService.queryProjInputList(map,"pub_default_sql","default_query_sql");
    	map.remove("sql");
    	//��ѯ���еĶ��ʽ
    	map.put("aguid", aguid);
    	List quotafomList = this.projManagerService.queryProjInputList(map, "ProjManager", "queryAllQuotafom");
    	for(int i=0;i<projTableList.size();i++){
    		Map projTabMap  = (Map)projTableList.get(i);
    		//�жϿ��ʽ Դ����ֵ�Ƿ����ı�
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
     * ���� ��λ��tableid ��ȡ���п��ʽ�� �����й�ʽ��ƴ��key value��ʽ����ҳ�� ��ҳ���������Ԫ��ֵ
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
     * ҳ����У������ķ���
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
     * ���������չʾ��TREELIST�б���� �����������ϸ��ı�ͷ��Ϣ
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
     * ��ѯ�������ϸ������
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
     * ����Ԥ�����������
     * */
    public void doChange() throws Exception{
    	String dataid = request.getParameter("dataid");
    	String tableid = request.getParameter("tableid");
    	String tabName = request.getParameter("tabName");
    	this.basicDataEntryService.doChange(dataid,tableid,tabName);
    }
    
    
    
    /**
     * ��ѯ������(Tabҳ������)
     * @return JSON
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public String showBuildData() throws Exception {
    	tablename = request.getParameter("tablename");
    	String mainId = request.getParameter("mainId"); 
    	//������ֶ�LXL
		String cols="";//Ҫ����ֶ�
		HashMap colInfos=getColInfo(tablename);//�õ����ݿ���ֶ������Ա�ת����� clob date
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
     * ��ѯ�����Ӧ��ϸ������
     * @return json
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public String showDetailData() throws Exception {
    	
    	HashMap map = new HashMap();
    	String mainId = request.getParameter("mainId"); 
    	map.put("mainId", mainId);
    	List list = this.budgetEditService.queryProjInputList(map, "ProjManager", "queryDetailData"); 
    	//����list��Ӻ�����֤��ʽ
    	rebuildListForValidate(list);
    	super.responseInterface(list);
    	
    	return null;
    }
    /**
     * ������װ�����ֶε�validateColumn
     * @param list
     * @throws OTSException
     */
	@SuppressWarnings("unchecked")
	private void rebuildListForValidate(List list) throws OTSException {
		for(int i=0;i<list.size();i++){
    		Map rebuildMap = (Map)list.get(i);
    		/**
    		 * 1.��ѯtb_pubexptypeeco��tb_pubexptypeecocsour��  ETEID�Ƿ��й�������
    		 * ����й�����ϵ��˵�����ʽ���Դ�����û�й�����ϵ���ж�isleafΪ0��¼1Ϊ����¼
    		 * 2.��ѯtb_pubexptypeecocsour��tb_pubexptypeecofom��isdefault�Ƿ�Ϊ1��
    		 * ���������ݵ�λ��ѯ��Ӧ��ʽ
    		 * 3.tb_pubexptypeecofomunit����λ�͹�ʽ�Ķ�Ӧ��ϵΪ���һ����һ����ʽ��Ӧ�����λ
    		 * 4.tb_pubquotafom��ʽ��
    		 * 5.tb_pubprocapitalsource�ʽ���Դ��
    		 * 6.
    		 */
    		String validateColumn = getValidateColumnValue(rebuildMap);
    		rebuildMap.put("validateColumn", validateColumn);
    	}
	}
	/**
	 * ǰ̨��������е��ò�ѯ��Ԫ����֤��Ϣ
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
	 * ��ȡ��������֤Ȩ�޵ķ�������
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
		//����list
		String validateColumn="";
		if(validateList3.size()<=0){
			validateList3 = validateList2;
		} 
		for(int j=0;j<validateList3.size();j++){
			Map validateMap = (Map)validateList3.get(j);
			//���ʽ���Դ���е���֤ƴ���ַ���
			if("".equals(validateColumn)){
				validateColumn = validateMap.get("PHYSICALNAME")+"="+ validateMap.get("QFID");
			}else{
				validateColumn = validateColumn +","+validateMap.get("PHYSICALNAME")+"="+ validateMap.get("QFID");
			}    			
		}
		return validateColumn;
	}

	/**
	 * ͨ��qidȡ�ö�Ӧ�Ķ��ʽֵ
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
		paraMap.put("sql", qf.getQfsql().replace("��WHERE��", " and aguid='"+aguid+"'"));
		Object retValue = this.dao.queryObject(paraMap, "pub_default_sql", "query_guid");
		paraMap.clear();
		paraMap.put("obj", retValue.toString());
		this.responseInterface(paraMap);
		return null;
	}
	 

	// ƴװ��Ŀ¼�������Ŀ����չʾ����
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
    
 // ���ݻ���id��ѯ��Ŀ������Ϣ
    @SuppressWarnings("unchecked")
	public String queryBudgetMain() {
    	try{
    		Map map = new HashMap();
        	String agencyid = request.getParameter("agencyid");
        	String year = request.getParameter("year");//���
        	String isChange = request.getParameter("isChange");//�Ƿ���ȵ���
        	map.put("agencyid", agencyid);
        	//List retList = this.budgetEditService.queryProjInputList(map,"BudgetEdit","query");
        	/*
        	//L:5.12�޸�
    		String cols="";//Ҫ����ֶ�
    		HashMap colInfos=getColInfo("TB_BUSI_BUDGETPROMAIN");//�õ����ݿ���ֶ������Ա�ת����� clob date
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
     * �������ݿ��б���ֶ�����
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
     * ��ѯ��Ŀ����ͷ
     * @return ��Ŀ���
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
     * ��ѯ��Ŀ��������
     * @return ��Ŀ���
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
     * ִ����Ŀ¼�뱣�����
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
    	String currentyear = request.getParameter("year");//���
    	String isChange =request.getParameter("isChanges");
    	
//����������ݵ����Ա�
//    	String sql = "insert into t_test (CREATEDATE,mianStr,detailStr,listStr,usercode ,BUSITYPE) " +
//    			"values ( to_char(sysdate,'YYYY/MM/DD HH24:MI:SS'),'"+mainData+"','"+detailData+"'," +
//    					"'"+listData+"','"+CUV.getUsercode()+"','Ԥ��౨')";
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
    	//���ǰ̨���ݹ���bpguid��ʹ��bpguid
    	if(!"".equals(bpguid)){
    		paramMap.put("bpguid", bpguid);
    	}   
    	
    	String   ischange=isChange.equals("")?"1":isChange;
    	
    	paramMap.put("ischange", ischange);
    	//����������Ϣ
    	Map retMainMap = new HashMap();
    	if(!"".equals(mainData)){
    		retMainMap = this.reportService.retXMLForm(mainData);//.retXmlMap("", mainData);
    	}
    	//������ϸ����Ϣ
		Map retDetailMap = new HashMap();//= this.reportService.retXMLList(detailData);//.retXmlMap("", detailData);
		if(!"".equals(detailData)){
			retDetailMap = this.reportService.retXMLList(detailData);
		}
		
    	//����ҽӱ����Ϣ
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
		
		String cols="";//Ҫ����ֶ�
		HashMap colInfos=getColInfo("TB_BUSI_BUDGETPROMAIN");//�õ����ݿ���ֶ������Ա�ת����� clob date
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
				// ��ȡHASHMAP�е�ÿ��KEY
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
     * ��ѯ��Ŀ����Ϣ���FREEFORM
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
     * ��ѯ��������
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public void showBudgetMainFormdata() throws Exception{
    	String bpguid = request.getParameter("ids");
		String cols="";//Ҫ����ֶ�
		HashMap colInfos=getColInfo("TB_BUSI_BUDGETPROMAIN");//�õ����ݿ���ֶ������Ա�ת����� clob date
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
     * ����������Ϣ Excel �ļ�
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
		String cols="";//Ҫ����ֶ�
		HashMap colInfos=getColInfo("TB_BUSI_BUDGETPROMAIN");//�õ����ݿ���ֶ������Ա�ת����� clob date
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
    	// ��ѯ������Ϣ
    	BudgetpromainVO budget = (BudgetpromainVO)budgetEditService.queryBudgetMain(map, "pub_default_sql", "queryBudgetMain");
    	budget.setYXXM(yxxm);
		budget.setPETCODE(petcode);
		budget.setXBBSJL(xbbsjl);
		budget.setEpcode(epcode);
		budget.setPROSTA(prosta);
		budget.setXMXZ(xmxz);
		budget.setXMJC(xmjc);
		budget.setMDCODE(mdcode);

    	// ��������
    	HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("sheet1");
		wb.setSheetName(0, "������Ϣ");
		HSSFHeader header = sheet.getHeader();
		header.setCenter("������Ϣ");
		// ���ñ�ͷ
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell((short) 0);
		cell.setCellValue("��ĿΨһ��");
		cell = row.createCell((short) 1);
		cell.setCellValue("��Ŀ���");
		cell = row.createCell((short) 2);
		cell.setCellValue("��Ŀ����");
		cell = row.createCell((short) 3);
		cell.setCellValue("��Ŀ����");
		cell = row.createCell((short) 4);
		cell.setCellValue("������Ŀ");
		cell = row.createCell((short) 5);
		cell.setCellValue("���ܿ�Ŀ");
		cell = row.createCell((short) 6);
		cell.setCellValue("��Ŀ״̬");
		cell = row.createCell((short) 7);
		cell.setCellValue("��Ŀ����");
		cell = row.createCell((short) 8);
		cell.setCellValue("��Ŀ����");
		cell = row.createCell((short) 9);
		cell.setCellValue("Ԥ����Ŀ����");
		cell = row.createCell((short) 10);
		cell.setCellValue("����ί��Ŀ����");
		cell = row.createCell((short) 11);
		cell.setCellValue("������ѡ��Ŀ����");
		cell = row.createCell((short) 12);
		cell.setCellValue("����������ű��");
		cell = row.createCell((short) 13);
		cell.setCellValue("���굥λһ�㹫��Ԥ��֧��С��");
		cell = row.createCell((short) 14);
		cell.setCellValue("���굥λ�����Ի���Ԥ��֧��С��");
		cell = row.createCell((short) 15);
		cell.setCellValue("���굥λһ�㹫��Ԥ��֧��С��");
		cell = row.createCell((short) 16);
		cell.setCellValue("���굥λ�����Ի���Ԥ��֧��С��");
		cell = row.createCell((short) 17);
		cell.setCellValue("���");
		cell = row.createCell((short) 18);
		cell.setCellValue("��Ŀʵʩ��λ(����)");
		cell = row.createCell((short) 19);
		cell.setCellValue("��ڿ���");
		cell = row.createCell((short) 20);
		cell.setCellValue("��Ŀ��ʼʱ��");
		cell = row.createCell((short) 21);
		cell.setCellValue("��Ŀ��ֹʱ��");
		cell = row.createCell((short) 22);
		cell.setCellValue("��Ŀ������");
		cell = row.createCell((short) 23);
		cell.setCellValue("��ϵ�绰 (�ֻ���)");
		cell = row.createCell((short) 24);
		cell.setCellValue("���");
		cell = row.createCell((short) 25);
		cell.setCellValue("��ע");
		cell = row.createCell((short) 26);
		cell.setCellValue("��Ŀ��λ��Ҫְ��");
		cell = row.createCell((short) 27);
		cell.setCellValue("��Ŀ�ſ�");
		cell = row.createCell((short) 28);
		cell.setCellValue("��Ŀ��������");
		cell = row.createCell((short) 29);
		cell.setCellValue("��Ŀ�����Ҫ��");
		cell = row.createCell((short) 30);
		cell.setCellValue("��Ŀ���������");
		cell = row.createCell((short) 31);
		cell.setCellValue("��Ŀ�ʽ�������");
		cell = row.createCell((short) 32);
		cell.setCellValue("��Ŀ���弨ЧĿ��");
		cell = row.createCell((short) 33);
		cell.setCellValue("��ȼ�Чָ����ϸ (����ָ��)");
		cell = row.createCell((short) 34);
		cell.setCellValue("��ȼ�Чָ����ϸ (Ч��ָ��)");
		cell = row.createCell((short) 35);
		cell.setCellValue("��ȼ�Чָ����ϸ (������������ָ��)");
		cell = row.createCell((short) 36);
		cell.setCellValue("����ԭ��");
		cell = row.createCell((short) 37);
		cell.setCellValue("������Ҫ˵������");
		//  ����������
		row = sheet.createRow(1);
		cell = row.createCell((short) 0);
		cell.setCellValue(budget.getBpguid());// ��Ŀ����
		cell = row.createCell((short) 1);
		cell.setCellValue(budget.getPETCODE());// ��Ŀ���
		cell = row.createCell((short) 2);
		cell.setCellValue(budget.getXBBSJL());// ��Ŀ����
		cell = row.createCell((short) 3);
		cell.setCellValue(budget.getBPNAME());// ��Ŀ����
		cell = row.createCell((short) 4);
		cell.setCellValue(budget.getYXXM());// ������Ŀ
		cell = row.createCell((short) 5);
		cell.setCellValue(budget.getEpcode());// ���ܿ�Ŀ
		cell = row.createCell((short) 6);
		cell.setCellValue(budget.getPROSTA());// ��Ŀ״̬
		cell = row.createCell((short) 7);
		cell.setCellValue(budget.getXMXZ());// ��Ŀ����
		cell = row.createCell((short) 8);
		cell.setCellValue(budget.getXMJC());// ��Ŀ����
		cell = row.createCell((short) 9);
		cell.setCellValue(budget.getBMXMKBH());// Ԥ����Ŀ����
		cell = row.createCell((short) 10);
		cell.setCellValue(budget.getFGWXMBM());// ����ί��Ŀ����
		cell = row.createCell((short) 11);
		cell.setCellValue(budget.getCZBXXMKBH());// ������ѡ��Ŀ����
		cell = row.createCell((short) 12);
		cell.setCellValue(budget.getNRCZAPBH());// ����������ű��
		cell = row.createCell((short) 13);
		cell.setCellValue(budget.getDYJDZC());// ���굥λһ�㹫��Ԥ��֧��С��
		cell = row.createCell((short) 14);
		cell.setCellValue(budget.getDEJDZC());// ���굥λ�����Ի���Ԥ��֧��С��
		cell = row.createCell((short) 15);
		cell.setCellValue(budget.getDSJDZC());// ���굥λһ�㹫��Ԥ��֧��С��
		cell = row.createCell((short) 16);
		cell.setCellValue(budget.getDSIJDZC());// ���굥λ�����Ի���Ԥ��֧��С��
		cell = row.createCell((short) 17);
		String deptorder = (null == budget.getDEPTORDER()? "" : budget.getDEPTORDER().toString());
		cell.setCellValue(deptorder);// ���
		cell = row.createCell((short) 18);
		cell.setCellValue(budget.getXMSSDW());// ��Ŀʵʩ��λ(����)
		cell = row.createCell((short) 19);
		cell.setCellValue(budget.getMDCODE());// ��ڿ���
		cell = row.createCell((short) 20);
		cell.setCellValue(budget.getBDATE());// ��Ŀ��ʼʱ��
		cell = row.createCell((short) 21);
		cell.setCellValue(budget.getEDATE());// ��Ŀ��ֹʱ��
		cell = row.createCell((short) 22);
		cell.setCellValue(budget.getXMFZR());// ��Ŀ������
		cell = row.createCell((short) 23);
		cell.setCellValue(budget.getLXDH());//��ϵ�绰 (�ֻ���)
		cell = row.createCell((short) 24);
		cell.setCellValue(budget.getXH());// ���
		cell = row.createCell((short) 25);
		cell.setCellValue(budget.getXMJJ());// ��ע
		cell = row.createCell((short) 26);
		cell.setCellValue(budget.getSLZB());// ��Ŀ��λ��Ҫְ��
		cell = row.createCell((short) 27);
		cell.setCellValue(budget.getZLZB());// ��Ŀ�ſ�
		cell = row.createCell((short) 28);
		cell.setCellValue(budget.getSXZB());// ��Ŀ��������
		cell = row.createCell((short) 29);
		cell.setCellValue(budget.getCBZB());// ��Ŀ�����Ҫ��
		cell = row.createCell((short) 30);
		cell.setCellValue(budget.getJJXYZB());// ��Ŀ���������
		cell = row.createCell((short) 31);
		cell.setCellValue(budget.getSHXYZB());// ��Ŀ�ʽ�������
		cell = row.createCell((short) 32);
		cell.setCellValue(budget.getHJXYZB());// ��Ŀ���弨ЧĿ��
		cell = row.createCell((short) 33);
		cell.setCellValue(budget.getKCXYXZB());// ��ȼ�Чָ����ϸ (����ָ��)
		cell = row.createCell((short) 34);
		cell.setCellValue(budget.getFWDXMYDZB());// ��ȼ�Чָ����ϸ (Ч��ָ��)
		cell = row.createCell((short) 35);
		cell.setCellValue(budget.getNDJXZBMX());// ��ȼ�Чָ����ϸ (������������ָ��)
		cell = row.createCell((short) 36);
		cell.setCellValue(budget.getDZYY());// ����ԭ��
		cell = row.createCell((short) 37);
		cell.setCellValue(budget.getQTXYSMSX());// ������Ҫ˵������
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
    	
		String nameString = java.net.URLEncoder.encode("������Ϣ.xls", "UTF-8");
		response.reset(); //����ײ��Ŀհ���
		response.setContentType("application/msexcel;charset=gbk"); //��������������ĵ�MIME���ͣ�Ĭ��Ϊtext/html
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
     * ����ѡ�е����������tabҳ��
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
     * ����ֻ��freeformbar��treelist
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
    	this.reportService.AddFFBtn("btn1", "����");
    	this.reportService.AddFFBtn("btn2", "����");
    	this.reportService.setFFBtn("btn1");
    	this.reportService.setFFBtn("btn2");
    	
    	String retXml = reportService.getReportTitle();
    	
     	super.responseInterface(retXml);
    	return null;
    }
    //ɾ����Ŀ����
    @SuppressWarnings("unchecked")
	public String delTabData() throws OTSException, IOException{
    	CaUserVO CUV=(CaUserVO)session.get("session_user");
    	String ip = request.getRemoteAddr();
    	String aguid = request.getParameter("aguid");
    	String bpguid = request.getParameter("bpguid");
    	String petcode = request.getParameter("petcode");
    	//�����ĿΪ��Ŀ���������ɾ������Ҫ�˻�Ϊ��ѡ״̬����ɾ��
    	if(petcode!=null&&!"".equals(petcode)&&!"null".equals(petcode)){
    		String sql = "select * from tb_pubproexptype t where t.petcode='"+petcode+"'";
    		Map map  = new HashMap();
    		map.put("sql", sql);
    		List list = this.budgetEditService.queryProjInputList(map, "pub_default_sql", "default_query_sql");
    		Map retMap = (Map)list.get(0);
    		String ispro = retMap.get("ISPRO").toString();
    		if("1".equals(ispro)){
    			retMap.clear();
    			retMap.put("message", "û��Ȩ��ɾ����Ŀ����Ŀ!");
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
	 * ���ݴ��ݽ����Ĳ���ִ��ͨ��SQL����ѯ��������
	 * 
	 * @param map ��ʱ��Ҫ�ֶ�ΪTABLENAME������
	 * @return �����б�
	 * @throws OTSException
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	public int createTable(Map map) throws OTSException {
		int a = 0;
		String physicalname = map.get("physicalname").toString();
		String max = map.get("max").toString();
		String max1 = String.valueOf((Integer.parseInt(max) + 1));
		String tableid = map.get("tableid").toString();
		//String sql = "create table " + physicalname + "(guid       VARCHAR2(38) default SYS_GUID(),agencyguid       VARCHAR2(38));\n comment on column " + physicalname + ".guid is \'Ψһ��ʶ\';\n comment on column " + physicalname + ".agencyguid  is \'��λ�����ʶ\';";
		String sql = "create table " + physicalname + "(guid       VARCHAR2(38) default SYS_GUID(),agencyguid       VARCHAR2(38))";
		String sqlinsert = "insert into tb_systablecols(colid, tableid, columnid, colname, physicalname, coldesc, coltype, iskey, cannull) values(" + max + ",\'" + tableid + "\',\'001\',\'Ψһ��ʶID\',\'guid\',\'Ψһ��ʶID\',\'003\',\'1\',\'0\')";
		String sqlinsert1 = "insert into tb_systablecols(colid, tableid, columnid, colname, physicalname, coldesc, coltype, iskey, cannull) values(" + max1 + ",\'" + tableid + "\',\'002\',\'���ű��\',\'agencyguid\',\'���ű��\',\'003\',\'0\',\'0\')";
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
	 * ���ݴ��ݽ����Ĳ���ִ��ͨ��SQL����ѯ��������
	 * 
	 * @param map ��ʱ��Ҫ�ֶ�ΪTABLENAME������
	 * @return �����б�
	 * @throws OTSException
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	public int dropTable(Map map) throws OTSException {
		int a = 0;
		String physicalname = map.get("physicalname").toString();
		String tableid = map.get("key").toString();
		//String sql = "create table " + physicalname + "(guid       VARCHAR2(38) default SYS_GUID(),agencyguid       VARCHAR2(38));\n comment on column " + physicalname + ".guid is \'Ψһ��ʶ\';\n comment on column " + physicalname + ".agencyguid  is \'��λ�����ʶ\';";
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
	 * ���ݴ��ݽ����Ĳ���ִ��ͨ��SQL����ѯ��������
	 * 
	 * @param map ��ʱ��Ҫ�ֶ�ΪTABLENAME������
	 * @return �����б�
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
     * ��ѯ������
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
     * ��ѯ������
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
    //ƴװtreexml
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
  //ƴ�Ŀe���x��
    @SuppressWarnings("unchecked")
	public  String tb_puboptionsXml(Map objListMap) {
		// ʹ�� DocumentHelper �ഴ��һ���ĵ�ʵ��
		Document document = DocumentHelper.createDocument();
		// ʹ�� addElement() ����������Ԫ�� TreeList
		Element dataElement = document.addElement("Data");

		Element itemsElement = dataElement.addElement("items");
		List list = (List)objListMap.get("list");
		Element itemElement1 = itemsElement.addElement("item");
		itemElement1.addAttribute("ID","000");
		itemElement1.addAttribute("str","����ҵ������");
		itemElement1.addAttribute("code","000");
   
		for(int i=0,len=list.size();i<len;i++){
			Map map = (HashMap)list.get(i);
 			if(map.get("OTYPE").toString().equals("004")){
 				//�� TreeList Ԫ����ʹ�� addElement() �������� Fonts Ԫ��
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
    
    
    //ƴ�Ŀe���x��
    @SuppressWarnings("unchecked")
	public  String treedataXml(Map objListMap) {
		// ʹ�� DocumentHelper �ഴ��һ���ĵ�ʵ��
		Document document = DocumentHelper.createDocument();
		// ʹ�� addElement() ����������Ԫ�� TreeList
		Element dataElement = document.addElement("Data");

		Element itemsElement = dataElement.addElement("items");
		List list = (List)objListMap.get("list");
		Element itemElement1 = itemsElement.addElement("item");
		itemElement1.addAttribute("ID","0");
		itemElement1.addAttribute("str","��Ŀ�������");
		itemElement1.addAttribute("code","000");
   
		for(int i=0,len=list.size();i<len;i++){
			Map map = (HashMap)list.get(i);
			//�� TreeList Ԫ����ʹ�� addElement() �������� Fonts Ԫ��
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
	 * ������ĿID��ѯ�Ƿ���Ŀ��ʶ
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
    	String year = request.getParameter("year");//���
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
