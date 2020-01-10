package sap_bi_restapi_package;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XML_helper {

	private CrashAndRunChecker crashruncheck;
	private Document doc;
	
	public XML_helper(CrashAndRunChecker tempcrashruncheck) {
		crashruncheck = tempcrashruncheck;
	}
	
	public Boolean parse_xml_file(String filename) {
		try {
			FileInputStream filestream = new FileInputStream(filename);		
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(filestream);
			return true;
		}
		catch (Exception ex) {
			crashruncheck.println("EXCEPTION CAUGHT IN class XML_helper method parse_xml_file !!!!!", ex);
			return false;
		}
	}
	
	public Boolean parse(String response) {
		
		try {
			ByteArrayInputStream instream = new ByteArrayInputStream(response.getBytes());		
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(instream);
			return true;
		}
		catch (Exception ex) {
			crashruncheck.println("EXCEPTION CAUGHT IN class XML_helper method parse !!!!!", ex);
			return false;
		}
        
        //doc.getDocumentElement().normalize();
        //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
		//String userName = doc.getDocumentElement().getAttribute("userName");
		//System.out.println("userName = " + userName);
		//String password = doc.getDocumentElement().getAttribute("password");
		//System.out.println("password = " + password);
		
        //doc.getDocumentElement().setAttribute("userName", "asdf");
		//doc.getDocumentElement().setAttribute("password", "asdf");
		
		//userName = doc.getDocumentElement().getAttribute("userName");
		//System.out.println("userName = " + userName);
		//password = doc.getDocumentElement().getAttribute("password");
		//System.out.println("password = " + password);
	}
	
	public void set_attr(String attrname, String value)
	{   
        Node attrs_node = doc.getElementsByTagName("attrs").item(0);
        NodeList attr_list = attrs_node.getChildNodes();
        for (int ii=0; ii < attr_list.getLength(); ii++) {
        	Node node = attr_list.item(ii);
        	if (node.getNodeType() == Node.ELEMENT_NODE) {
        		Element eElement = (Element) node;
        		if ("attr".equals(eElement.getNodeName())) {
        			
        			String name = eElement.getAttribute("name");
        			
        			if (name.equals(attrname))
        			{
        				eElement.setTextContent(value);
        				return;
        			}
        			
        		}
        	}
        }
	}//set_attr
	
	public String get_attr(String attrname)
	{
		Node attrs_node = doc.getElementsByTagName("attrs").item(0);
        NodeList attr_list = attrs_node.getChildNodes();
        for (int ii=0; ii < attr_list.getLength(); ii++) {
        	Node node = attr_list.item(ii);
        	if (node.getNodeType() == Node.ELEMENT_NODE) {
        		Element eElement = (Element) node;
        		if ("attr".equals(eElement.getNodeName())) {
        			
        			String name = eElement.getAttribute("name");
        			
        			if (name.equals(attrname))
        			{
        				String value = eElement.getTextContent();
        				return value;
        			}
        			
        		}
        	}
        }
        
        return null;
	}//get_attr
	
	
	public ArrayList<String> Parse_UserGroup_Entry_Elements(String xmlbodystr) {
		Boolean parse_ok = this.parse(xmlbodystr);
		if (parse_ok == false)
			return null;
		
		ArrayList<String> myresults = new ArrayList<String>();
		
		NodeList entrylist = doc.getElementsByTagName("entry");
		int length = entrylist.getLength();
		for (int el=0; el < length; el++) {
			Node entry = entrylist.item(el);
			NodeList entry_kids = entry.getChildNodes();
			for (int ek=0; ek < entry_kids.getLength(); ek++) {
				Node entry_kid = entry_kids.item(ek);
				if (entry_kid.getNodeName().equals("content")) {
					NodeList content_kids = entry_kid.getChildNodes();
					for (int ck=0; ck < content_kids.getLength(); ck++) {
						Node content_kid = content_kids.item(ck);
						if (content_kid.getNodeName().equals("attrs")) {
							NodeList attrs_kids = content_kid.getChildNodes();
							for (int ak=0; ak < attrs_kids.getLength(); ak++) {
								Node attrs_kid = attrs_kids.item(ak);
								if (attrs_kid.getNodeName().equals("attr")) {
									Element attr = (Element) attrs_kid;
									if (attr.getAttribute("name").equals("id")) {
										String usergroup_id_str = attr.getTextContent();
										myresults.add(usergroup_id_str);
									}
								}
							}
						}
					}
				}
			}
		}
		
		return myresults;
	}//Parse_UserGroup_Entry_Elements
	
	
	//**************************************************************************************************************************
	//input:	tempstrarray = only add entries to list with strings containing these substrings
	//			mylist == ArrayList<InfostoreTreeNode> (this gets added to)
	//
	//output:   integer == total number of XML-entry-nodes found
	//          (NOTE: NOT number of InfostoreTreeNode objects added to mylist like in method infostore_page_parse !!!!!)
	//**************************************************************************************************************************
	public int infostore_subfolders_page_parse(boolean case_sensitive_search, String[] tempstrarray, ArrayList<InfostoreTreeNode> mylist) {
		int entrycount = 0;
		Node main_node = doc.getElementsByTagName("feed").item(0);
		NodeList child_list = main_node.getChildNodes();
		for (int ii=0; ii < child_list.getLength(); ii++) {
			Node mychild = child_list.item(ii);
			short mychildnodetype = mychild.getNodeType();
			if (mychildnodetype == Node.ELEMENT_NODE) {
				Element mychildelem = (Element) mychild;
				if ("entry".equals(mychildelem.getNodeName())) {
					
					entrycount++;
					
					InfostoreTreeNode entry = new InfostoreTreeNode();
					
					NodeList inside_list = mychildelem.getChildNodes();
					for (int xx=0; xx < inside_list.getLength(); xx++) { 
        			
						String name = inside_list.item(xx).getNodeName();
						
						if (name.equals("author")) {
							NodeList author_children = inside_list.item(xx).getChildNodes();
							for (int aa=0; aa < author_children.getLength(); aa++) {
								String author_child_name = author_children.item(aa).getNodeName();
								if (author_child_name.equals("name"))
									entry.author_name = author_children.item(aa).getTextContent();
								else if (author_child_name.equals("uri"))
									entry.author_uri = author_children.item(aa).getTextContent();
							}
						}
						else if (name.equals("link")) {
							Element linkelem = (Element)inside_list.item(xx);
							entry.link = linkelem.getAttribute("href");
						}
						else if (name.equals("content")) {
							NodeList content_children = inside_list.item(xx).getChildNodes();
							//"content" node should have 1 and only 1 child named "attrs"
							Node attrs_node = content_children.item(0);
							NodeList attrs_children = attrs_node.getChildNodes();
							for (int ac=0; ac < attrs_children.getLength(); ac++) {
								String attrs_child_name = attrs_children.item(ac).getNodeName();
								if (attrs_child_name.equals("attr")) {
									Element attrelem = (Element)(attrs_children.item(ac));
									String tempname = attrelem.getAttribute("name");
									if (tempname.equals("id"))
										entry.id = attrelem.getTextContent();
									else if (tempname.equals("cuid"))
										entry.cuid = attrelem.getTextContent();
									else if (tempname.equals("description"))
										entry.description = attrelem.getTextContent();
									else if (tempname.equals("name"))
										entry.name = attrelem.getTextContent();
									else if (tempname.equals("type"))
										entry.type = attrelem.getTextContent();
								}
							}
						}
					}//for inside_list
					
					//*************************************************************************************************
					//arraylist add only if filtered by tempstrarray otherwise the object will get garbage-collected
					//*************************************************************************************************
					for (int tt=0; tt < tempstrarray.length; tt++) {
						String filterstr = tempstrarray[tt].trim();
						if (filterstr.length() > 0) {
							if (filterstr.equals("*")) {
								mylist.add(entry);
								break;
							}
							else {
								if (case_sensitive_search) { //for example: John does NOT match john
									if (entry.name.contains(filterstr)) {
										mylist.add(entry);
										break;
									}
								}
								else { //case insensitive search, for example: John does match john
									if (entry.name.toLowerCase().contains(filterstr.toLowerCase())) {
										mylist.add(entry);
										break;
									}
								}
							}
						}
					}
        		}//if entry
			}//if Node.ELEMENT_NODE
		}//for childlist
		
		return entrycount;
	}//infostore_subfolders_page_parse
	
	
	
	//****************************************************************************
	//input: mylist == ArrayList<InfostoreTreeNode>
	//list is modified
	//
	//output: integer == number of InfostoreTreeNode objects added to mylist
	//****************************************************************************
	public int infostore_page_parse(ArrayList<InfostoreTreeNode> mylist) {
		int entrycount = 0;
		Node main_node = doc.getElementsByTagName("feed").item(0);
		NodeList child_list = main_node.getChildNodes();
		for (int ii=0; ii < child_list.getLength(); ii++) {
			Node mychild = child_list.item(ii);
			short mychildnodetype = mychild.getNodeType();
			if (mychildnodetype == Node.ELEMENT_NODE) {
				Element mychildelem = (Element) mychild;
				if ("entry".equals(mychildelem.getNodeName())) {
					
					InfostoreTreeNode entry = new InfostoreTreeNode();
					
					NodeList inside_list = mychildelem.getChildNodes();
					for (int xx=0; xx < inside_list.getLength(); xx++) {
        			
						String name = inside_list.item(xx).getNodeName();
						
						if (name.equals("author")) {
							NodeList author_children = inside_list.item(xx).getChildNodes();
							for (int aa=0; aa < author_children.getLength(); aa++) {
								String author_child_name = author_children.item(aa).getNodeName();
								if (author_child_name.equals("name"))
									entry.author_name = author_children.item(aa).getTextContent();
								else if (author_child_name.equals("uri"))
									entry.author_uri = author_children.item(aa).getTextContent();
							}
						}
						else if (name.equals("link")) {
							Element linkelem = (Element)inside_list.item(xx);
							entry.link = linkelem.getAttribute("href");
						}
						else if (name.equals("content")) {
							NodeList content_children = inside_list.item(xx).getChildNodes();
							//"content" node should have 1 and only 1 child named "attrs"
							Node attrs_node = content_children.item(0);
							NodeList attrs_children = attrs_node.getChildNodes();
							for (int ac=0; ac < attrs_children.getLength(); ac++) {
								String attrs_child_name = attrs_children.item(ac).getNodeName();
								if (attrs_child_name.equals("attr")) {
									Element attrelem = (Element)(attrs_children.item(ac));
									String tempname = attrelem.getAttribute("name");
									if (tempname.equals("id"))
										entry.id = attrelem.getTextContent();
									else if (tempname.equals("cuid"))
										entry.cuid = attrelem.getTextContent();
									else if (tempname.equals("description"))
										entry.description = attrelem.getTextContent();
									else if (tempname.equals("name"))
										entry.name = attrelem.getTextContent();
									else if (tempname.equals("type"))
										entry.type = attrelem.getTextContent();
								}
							}
						}
					}
					
					//**************
					//arraylist add
					//**************
					mylist.add(entry);
					entrycount++;
        		}//if entry
			}//if Node.ELEMENT_NODE
		}//for childlist
		
		return entrycount;
	}//infostore_page_parse
	
	
	
	
	//***********************************************************************************************************
	//returns: ArrayList<DocumentRefresh_Parameters.Parameter> (this is added to if any parameters)
	//***********************************************************************************************************
	public ArrayList<DocumentRefresh_Parameter> document_refresh_parameters_parse() {
		
		ArrayList<DocumentRefresh_Parameter> myparams = new ArrayList<DocumentRefresh_Parameter>();

		Node main_node = doc.getElementsByTagName("parameters").item(0);
		NodeList child_list = main_node.getChildNodes();
		for (int ii=0; ii < child_list.getLength(); ii++) {
			Node mychild = child_list.item(ii);
			short mychildnodetype = mychild.getNodeType();
			if (mychildnodetype == Node.ELEMENT_NODE) {
				Element mychildelem = (Element) mychild;
				if ("parameter".equals(mychildelem.getNodeName())) {
					
					DocumentRefresh_Parameter param = new DocumentRefresh_Parameter();
					
					param.parameter_type     = mychildelem.getAttribute("type");
					param.parameter_optional = mychildelem.getAttribute("optional");
					
					NodeList inside_list = mychildelem.getChildNodes();
					for (int xx=0; xx < inside_list.getLength(); xx++) {
        			
						String name = inside_list.item(xx).getNodeName();
						
						if (name.equals("id")) {
							param.parameter_id = inside_list.item(xx).getTextContent();
						}
						else if (name.equals("name")) {
							param.parameter_name = inside_list.item(xx).getTextContent();
						}
						else if (name.equals("answer")) {
							Element answer_elem = (Element)inside_list.item(xx);
							param.answer_type = answer_elem.getAttribute("type");
							
							NodeList answer_children = answer_elem.getChildNodes();
							for (int aa=0; aa < answer_children.getLength(); aa++) {
								String answer_child_name = answer_children.item(aa).getNodeName();
								if (answer_child_name.equals("values")) {
									
									param.answer_values = new ArrayList<String>();
									
									Element values_elem = (Element)answer_children.item(aa);
									NodeList values_kids = values_elem.getChildNodes();
									for (int vk=0; vk < values_kids.getLength(); vk++) {
										String values_kid_name = values_kids.item(vk).getNodeName();
										if (values_kid_name.equals("value"))
											param.answer_values.add(values_kids.item(vk).getTextContent());
									}
								}
								else if (answer_child_name.equals("info")) {
									
									Element info_elem = (Element)answer_children.item(aa);
									param.info_cardinality = info_elem.getAttribute("cardinality");
									
									NodeList info_children = info_elem.getChildNodes();
									for (int yy=0; yy < info_children.getLength(); yy++) {
										String info_child_name = info_children.item(yy).getNodeName();
										if (info_child_name.equals("values")) {
											Element values_elem = (Element) info_children.item(yy);
											
											param.info_values = new ArrayList<String>();
											
											NodeList values_children = values_elem.getChildNodes();
											for (int zz=0; zz < values_children.getLength(); zz++) {
												String values_child_name = values_children.item(zz).getNodeName();
												if (values_child_name.equals("value")) {
													Element myvalue = (Element)values_children.item(zz);
													String val = myvalue.getTextContent();
													param.info_values.add(val);
												}
											}
										}
										else if (info_child_name.equals("lov")) {
											Element lov_elem = (Element) info_children.item(yy);
											
											param.info_lov_refreshable  = lov_elem.getAttribute("refreshable");
											param.info_lov_partial      = lov_elem.getAttribute("partial");
											param.info_lov_hierarchical = lov_elem.getAttribute("hierarchical");
											
											NodeList lov_children = lov_elem.getChildNodes();
											for (int vv=0; vv < lov_children.getLength(); vv++) {
												
												String lov_child_name = lov_children.item(vv).getNodeName();
												if (lov_child_name.equals("cvalues")) {
													
													Element cvalues_elem = (Element) lov_children.item(vv);
													NodeList cvalues_children = cvalues_elem.getChildNodes();
													param.info_lov_cvalues = cvalues_children;
												}//cvalues
												else if (lov_child_name.equals("columns")) {
													
													param.info_lov_columns_type = new ArrayList<String>();
													param.info_lov_columns_textname = new ArrayList<String>();
													
													Element columns_elem = (Element)lov_children.item(vv);
													
													param.info_lov_columns_mappingId = columns_elem.getAttribute("mappingId");
													
													NodeList columns_children = columns_elem.getChildNodes();
													for (int cc=0; cc < columns_children.getLength(); cc++) {
														
														String columns_child_name = columns_children.item(cc).getNodeName();
														if (columns_child_name.equals("column")) {
															
															Element col_elem = (Element)columns_children.item(cc);
															param.info_lov_columns_type.add(col_elem.getAttribute("type"));
															param.info_lov_columns_textname.add(col_elem.getTextContent());
															
														}//column
													}//columns_children
												}//columns
												else if (lov_child_name.equals("intervals")) {
													
													Element intervals_elem = (Element)lov_children.item(vv);
													NodeList intervals_children = intervals_elem.getChildNodes();
													param.info_lov_intervals = intervals_children;
												}//intervals
												else if (lov_child_name.equals("values")) {
													
													param.info_lov_values = new ArrayList<String>();
													
													Element values_elem = (Element)lov_children.item(vv);
													NodeList values_children = values_elem.getChildNodes();
													for (int cc=0; cc < values_children.getLength(); cc++) {
														String values_child_name = values_children.item(cc).getNodeName();
														if (values_child_name.equals("value")) {
															Element value_elem = (Element)values_children.item(cc);
															param.info_lov_values.add(value_elem.getTextContent());
														}
													}
												}
											}//lov_children
										}//lov
									}//info_children
								}//info
							}//answer_children
						}//answer
					}//inside_list
					
					//**************
					//arraylist add
					//**************
					myparams.add(param);

        		}//if param
			}//if Node.ELEMENT_NODE
		}//for childlist
		
		return myparams;
		
	}//document_refresh_parameters_parse
	
	
	
	
	//**************************************************************************************************************************************
	//input:  paramID    == integer ID of parameter
	//        intervalID == integer ID of interval
	//
	//output: null     == something went wrong
	//        not-null == NodeList of kids under <lov> for this intervalID of this paramID
	//**************************************************************************************************************************************
	public NodeList parse_LOV_Interval(String paramID, String intervalID) {
		
		Node main_node = doc.getElementsByTagName("parameters").item(0);
		NodeList child_list = main_node.getChildNodes();
		for (int ii=0; ii < child_list.getLength(); ii++) {
			Node mychild = child_list.item(ii);
			short mychildnodetype = mychild.getNodeType();
			if (mychildnodetype == Node.ELEMENT_NODE) {
				Element mychildelem = (Element) mychild;
				if ("parameter".equals(mychildelem.getNodeName())) {
					
					//looking for paramID
					NodeList inside_list = mychildelem.getChildNodes();
					for (int xx=0; xx < inside_list.getLength(); xx++) {
        			
						String name = inside_list.item(xx).getNodeName();
						
						if (name.equals("id")) {
							String temp = inside_list.item(xx).getTextContent();
							if (temp.equals(paramID)) { //found target param
						
								NodeList param_kids = mychildelem.getChildNodes();
								for (int pp=0; pp < param_kids.getLength(); pp++) {
									if (param_kids.item(pp).getNodeName().equals("answer")) {
										Element answer_elem = (Element)param_kids.item(pp);
										NodeList answer_kids = answer_elem.getChildNodes();
										for (int aa=0; aa < answer_kids.getLength(); aa++) {
											if (answer_kids.item(aa).getNodeName().equals("info")) {
												Element info_elem = (Element) answer_kids.item(aa);
												NodeList info_kids = info_elem.getChildNodes();
												for (int ik=0; ik < info_kids.getLength(); ik++) {
													if (info_kids.item(ik).getNodeName().equals("lov")) {
														Element lov_elem = (Element) info_kids.item(ik);
														NodeList lov_kids = lov_elem.getChildNodes();
														return lov_kids;
													}
												}
											}
										}
									}
								}
							}//found target param
						}//id
					}//inside_list
        		}//if param
			}//if Node.ELEMENT_NODE
		}//for childlist
		
		return null;
	}//parse_LOV_Interval
	
	
	//************************************************************************************************************************************************
	//returns:    ""    == some form of error where xml body could not be created
	//returns: xml_body == on success returns xml body as a String
	//
	//Put user choices inside <answer></answer> after <info></info> encapsulated by <values></values> and each choice gets <value></value>
	//************************************************************************************************************************************************
	public String  Write_Document_Refresh_Parameters(ArrayList<DocumentRefresh_Parameter> parameter_definitions, List<String>[] list_params, Parameter_Date_Button[] date_params, List<String>[] text_params) {
		
		int param_index = -1;
		
		Node main_node = doc.getElementsByTagName("parameters").item(0);
		NodeList child_list = main_node.getChildNodes();
		for (int ii=0; ii < child_list.getLength(); ii++) {
			Node mychild = child_list.item(ii);
			short mychildnodetype = mychild.getNodeType();
			if (mychildnodetype == Node.ELEMENT_NODE) {
				Element mychildelem = (Element) mychild;
				if ("parameter".equals(mychildelem.getNodeName())) {
					
					param_index++;
					
					NodeList inside_list = mychildelem.getChildNodes();
					for (int xx=0; xx < inside_list.getLength(); xx++) {
        			
						String name = inside_list.item(xx).getNodeName();
						
						if (name.equals("answer")) {
							Element answer_elem = (Element)inside_list.item(xx);
							
							//*******************************************************************************
							//Inside <interval></interval> it can be <cvalue> or <value> for example:
							
                            //Example: <interval><cvalue>.....</cvalue></interval>
                            //So info_lov_cvalues should NOT BE NULL

                            //Example: <interval><value>.....</value></interval>
                            //So info_lov_values should NOT BE NULL
							//*******************************************************************************
							
							//docs say: 
							//appendChild
							//Adds the node newChild to the end of the list of children of this node. If the newChild is already in the tree, it is first removed.
							//So we need to search for any existing "values" element or "cvalues" element.
							
							Element values_elem = null;
							NodeList answer_children = answer_elem.getChildNodes();
							for (int aa=0; aa < answer_children.getLength(); aa++) {
								String answer_child_name = answer_children.item(aa).getNodeName();
								if (answer_child_name.equals("values"))
									values_elem = (Element)answer_children.item(aa);
							}
							
							if (values_elem != null)
								answer_elem.removeChild(values_elem);
							
							//check to see if we should insert values for this parameter
							//for DateTime currently we are only support single-select if we encounter multiple-select on a report we have to change this code, we show a JOptionsPane if we encounter it
							DocumentRefresh_Parameter param = parameter_definitions.get(param_index);
							if (param.answer_type.equals("DateTime")) { //date params
								String datestr = date_params[param_index].getDateAsString();
								if (datestr.length() > 0) {  //mm/dd/yyyy
									String[] strarray = datestr.split("/"); //split into mm dd yyyy delimited by "/" 
									String month = null;
									if (strarray[0].trim().length() == 2)
										month = strarray[0];
									else
										month = "0" + strarray[0];
									
									String day = null;
									if (strarray[1].trim().length() == 2)
										day = strarray[1];
									else
										day = "0" + strarray[1];
									
									String year = "";
									String baseyear = strarray[2].trim();
									int baseyearlen = baseyear.length();
									if (baseyearlen == 4)
										year = baseyear;
									else {
										int zerolen = 4 - baseyearlen;
										for (int zz=0; zz < zerolen; zz++)
											year += "0";
										year += baseyear;
									}
									
									String mynewdatestr = String.format("%s-%s-%sT14:00:00.000Z", year, month, day);
									
									Element value  = doc.createElement("value");
									value.setTextContent(mynewdatestr);
									
									values_elem = doc.createElement("values");
									values_elem.appendChild(value);
									
									answer_elem.appendChild(values_elem);
								}
							}
							else if (param.is_text_button_parameter()) { //text parameter
								values_elem = doc.createElement("values");
								
								for (int text_params_index=0; text_params_index < text_params[param_index].size(); text_params_index++) {
									Element value = doc.createElement("value");
									value.setTextContent(text_params[param_index].get(text_params_index));
									values_elem.appendChild(value);
								}
								
								answer_elem.appendChild(values_elem);
							}
							else { //list_params
								ArrayList<String> selections = (ArrayList<String>)list_params[param_index];
								if (selections != null) {
									if (selections.size() > 0) {
										
										//*******************************************************************************
										//Inside <interval></interval> it can be <cvalue> or <value> for example:
										
			                            //Example: <interval><cvalue>.....</cvalue></interval>
			                            //So info_lov_cvalues should NOT BE NULL

			                            //Example: <interval><value>.....</value></interval>
			                            //So info_lov_values should NOT BE NULL
										//*******************************************************************************
										
										values_elem = doc.createElement("values");
										
										for (int ss=0; ss < selections.size(); ss++) {
											
											String mapping_str = null;
											
											if (param.info_lov_intervals != null) {
												
												if (param.info_lov_cvalues != null) {
													//*******************************************************
													//Example: <interval><cvalue>.....</cvalue></interval>
                                                    //So info_lov_cvalues should NOT BE NULL
													//*******************************************************
													int column_count = param.info_lov_columns_textname.size();
													String[] strarray = selections.get(ss).split("-");
													if (strarray.length != column_count) {
														String debugstr = String.format("Write_Document_Refresh_Parameters: info_lov_intervals NOT NULL, info_lov_cvalues NOT NULL ..... param_index=%d ss=%d has dashes in the names so string split will fail code needs to be changed !!!!!", param_index, ss);
														crashruncheck.println(debugstr);
														return "";
													}
													else {
														Integer map_id = Integer.parseInt(param.info_lov_columns_mappingId);
														mapping_str = strarray[map_id].trim();
													}
												}
												else if (param.info_lov_values != null) {
													//********************************************************
													//Example: <interval><value>.....</value></interval>
                                                    //So info_lov_values should NOT BE NULL
													//********************************************************
													mapping_str = selections.get(ss);
												}
												else {
													String debugstr = String.format("Write_Document_Refresh_Parameters: param_index=%d ss=%d found a list parameter that has info_lov_intervals NOT NULL, BUT NO info_lov_cvalues OR info_lov_values !!!!! How can this be ..... code needs to be changed !!!!!", param_index, ss);
													crashruncheck.println(debugstr);
													return "";
												}
												
											}
											else if (param.info_lov_cvalues != null) {
												int column_count = param.info_lov_columns_textname.size();
												String[] strarray = selections.get(ss).split("-");
												if (strarray.length == column_count) {
													Integer map_id = Integer.parseInt(param.info_lov_columns_mappingId);
													mapping_str = strarray[map_id].trim();
												}
												else {
													String debugstr = String.format("Write_Document_Refresh_Parameters: info_lov_cvalues NOT NULL ..... param_index=%d ss=%d has dashes in the names so string split will fail code needs to be changed !!!!!", param_index, ss);
													crashruncheck.println(debugstr);
													return "";
												}
											}
											else if (param.info_values != null)
												mapping_str = selections.get(ss);
											else {
												String debugstr = String.format("Write_Document_Refresh_Parameters: param_index=%d ss=%d found a list parameter that is not using info_values, info_lov_intervals, or info_lov_cvalues !!!!! How can this be ..... code needs to be changed !!!!!", param_index, ss);
												crashruncheck.println(debugstr);
												return "";
											}
											
											
											Element value = doc.createElement("value");
											value.setTextContent(mapping_str);
											values_elem.appendChild(value);
											
										}//for ss
										
										answer_elem.appendChild(values_elem);
									}//selections.size > 0
								}//selections NOT NULL
							}//list params
							
							break;
						}//answer
					}//for inside_list
				}//parameter
			}//ELEMENT_NODE
		}//for child_list
		
		String xmlbodystr = transform_to_String();
		
		return xmlbodystr;
	}//Write_Document_Refresh_Parameters
	
	
	public String transform_to_String() {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource xmlsource = new DOMSource(doc);
		
			ByteArrayOutputStream tempout = new ByteArrayOutputStream();
			StreamResult xmlresult = new StreamResult(tempout);
			transformer.transform(xmlsource, xmlresult);
			String result = tempout.toString();
			tempout.close();
			
			return result;
		}
		catch (Exception ex)
		{
			crashruncheck.println("EXCEPTION CAUGHT IN class XML_helper method transform_to_String !!!!!", ex);
			String msg = ex.getMessage();
			System.out.println(msg);
			return "";
		}
	}

}
