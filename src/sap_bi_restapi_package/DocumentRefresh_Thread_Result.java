package sap_bi_restapi_package;

import java.util.ArrayList;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class DocumentRefresh_Thread_Result {
	
	public CrashAndRunChecker                   crashruncheck;
	public ArrayList<DocumentRefresh_Parameter>	documentRefresh_SaveParameters;
	public XML_helper							documentRefresh_ParsedXML;
	public String[][]							list_param_choices;
	public String[]								list_param_labels;
	public Integer								list_params_get_choices;
	
	private SAP_BI_WebIntel_REST_helper         my_rest_caller;
	private String                              docidstr;
	
	public DocumentRefresh_Thread_Result(SAP_BI_WebIntel_REST_helper temprest, CrashAndRunChecker tempcrash, String tempdocidstr) {
		
		my_rest_caller                  = temprest;
		crashruncheck                   = tempcrash;
		docidstr                        = tempdocidstr;
		
		documentRefresh_SaveParameters	= null;
		documentRefresh_ParsedXML		= null;
		list_param_choices				= null;
		list_param_labels				= null;
		list_params_get_choices         = null;
	}
	
	
	//*******************************************************************************************************************************
	//list_param_get_choices:
	// 0 = success, found the list of string values for this intervalID of this paramID
	//-1 = found this list of string values for this intervalID of this paramID but the start and end values are not matching
	//-2 = could not find the list of string values for this intervalID of this paramID this code needs to be stepped thru
	//-3 = responseCode that is not 200 from rest-api call
	//-4 = caught a throwable exception need to check crashruncheck text file to debug
	//*******************************************************************************************************************************
	public void Get_All_List_Param_Choices() {
		
		list_param_choices  = new String[documentRefresh_SaveParameters.size()][];
		list_param_labels   = new String[documentRefresh_SaveParameters.size()];
		for (int pp=0; pp < documentRefresh_SaveParameters.size(); pp++) {
			
			DocumentRefresh_Parameter param = documentRefresh_SaveParameters.get(pp);
			
			list_param_labels[pp]  = "";
			list_param_choices[pp] = null;
			
			if (param.answer_type.equals("Numeric") || param.answer_type.equals("Text")) {
				
				if (param.info_lov_partial != null && param.info_lov_partial.equals("false")) { //lov

					//************************************************************************************************************
					//info_lov_intervals has priority
					//because you may have info_lov_intervals AND info_lov_cvalues as in
					//doc_id=19576 on parameter=6 /FIN/FIN_C2HERPS/FIN-GL-0013b Expenditure Transaction Detail
					//where children of info_lov_intervals contain "cvalue xml-elements"
					//unlike 
					//doc_id=592369 parameter=3 /Development/Maxwell/maxtest_FIN-DEBT-HON0030 Debt Registry Balances by Series
					//which contains only info_lov_intervals BUT NO info_lov_cvalues
					//and children of info_lov_intervals contain "value xml-elements"
					//************************************************************************************************************
					
					if (param.info_lov_intervals != null) {
						
						//****************************************************
						//label of param button depends on info_lov_cvalues
						//****************************************************
						if (param.info_lov_cvalues != null) { 
							int cvalue_elem_len = param.info_lov_columns_textname.size();
							for (int ii=0; ii < cvalue_elem_len; ii++) {
								if (ii > 0)
									list_param_labels[pp] += "-";
								
								list_param_labels[pp] += param.info_lov_columns_textname.get(ii).trim();
							}
						}
						else
							list_param_labels[pp] = param.info_lov_columns_textname.get(0);
						
						
						//<interval> children
						ArrayList<String> values = new ArrayList<String>();
						int info_lov_intervals_length = param.info_lov_intervals.getLength();
						for (int cc=0; cc < info_lov_intervals_length; cc++) {
							if (param.info_lov_intervals.item(cc).getNodeName().equals("interval")) {
								
								//*****************************************************************************************************************************************
								//kids of <interval> can be
								//<cvalue> elements like in docid=19576  parameter=6 /FIN/FIN_C2HERPS/FIN-GL-0013b Expenditure Transaction Detail
								//or
								//<value>  elements like in docid=592369 parameter=3 /Development/Maxwell/maxtest_FIN-DEBT-HON0030 Debt Registry Balances by Series
								//*****************************************************************************************************************************************
								
								Element  interval_elem        = (Element)param.info_lov_intervals.item(cc);
								String   interval_id          = interval_elem.getAttribute("id");
								
								NodeList interval_kids        = interval_elem.getChildNodes();
								int      interval_kids_length = interval_kids.getLength();
								
								boolean  get_start            = true;
								String   start_value          = null;
								String   end_value            = null;
								for (int ik=0; ik < interval_kids_length; ik++) {
								
										Node mynode = interval_kids.item(ik);
										if (mynode.getNodeName().equals("cvalue")) {
											
											//******************************************************
											//Example: <interval><cvalue>.....</cvalue></interval>
											//So info_lov_cvalues should NOT BE NULL
											//******************************************************
											
											//contains <column> children
											NodeList cvalue_kids = mynode.getChildNodes();
											ArrayList<String> column_textcontent = new ArrayList<String>();
											//ArrayList<String> column_id          = new ArrayList<String>();
											
											for (int ck=0; ck < cvalue_kids.getLength(); ck++) {
												if (cvalue_kids.item(ck).getNodeName().equals("column")) {
													String temp_column_str = cvalue_kids.item(ck).getTextContent();
													column_textcontent.add(temp_column_str);
													
													//Element temp_elem = (Element) cvalue_kids.item(ck);
													//String temp_column_id = temp_elem.getAttribute("id");
													//column_id.add(temp_column_id);
												}
											}
											
											//*************************************************
											//ok start_value and end_value depend on 
											//param.info_lov_columns_mappingId
											//param.info_lov_columns_type
											//param.info_lov_columns_textname
											//*************************************************
											if (get_start) {
												get_start   = false;
												int map_id  = Integer.parseInt(param.info_lov_columns_mappingId);
												start_value = column_textcontent.get(map_id);
											}
											else {
												get_start = true;
												int map_id  = Integer.parseInt(param.info_lov_columns_mappingId);
												end_value   = column_textcontent.get(map_id);
												
												int max_column_count = param.info_lov_columns_type.size();
												list_params_get_choices = this.Call_And_Parse_LOV_interval_CVALUE_elements(param.parameter_id, interval_id, start_value, end_value, map_id, values, max_column_count);
												if (list_params_get_choices != 0)
													return;
											}
										}
										else if (mynode.getNodeName().equals("value")) {
											
											//*******************************************************
											//Example: <interval><value>.....</value></interval>
											//So info_lov_values should NOT BE NULL
											//*******************************************************
											
											//there needs to be 2 <value> elements: start-value and end-value for query
											if (get_start) {
												get_start = false;
												start_value = mynode.getTextContent();
											}
											else {
												get_start = true;
												end_value = mynode.getTextContent();
												
												list_params_get_choices = this.Call_And_Parse_LOV_interval_value_elements(param.parameter_id, interval_id, start_value, end_value, values);
												if (list_params_get_choices != 0)
													return;
											}
											
										}
								}//interval_kids
							}//interval
						}//info_lov_intervals
							
						list_param_choices[pp] = values.toArray(new String[values.size()]);
						
					}//intervals
					else if (param.info_lov_cvalues != null) { 
						int cvalue_elem_len = param.info_lov_columns_textname.size();
						for (int ii=0; ii < cvalue_elem_len; ii++) {
							if (ii > 0)
								list_param_labels[pp] += "-";
							
							list_param_labels[pp] += param.info_lov_columns_textname.get(ii).trim();
						}
						
						//Remember parameter=6 of doc_id=19576 which is /FIN/FIN_C2HERPS/FIN-GL-0013b Expenditure Transaction Detail
						//the cvalue after 0159 Leasehold Conversion Program there is only 1 column value when everyone else has 2 column values
						//we are going to make it possible to add "" to templist to make array size good.
						
						ArrayList<String> templist = new ArrayList<String>();
						for (int cc=0; cc < param.info_lov_cvalues.getLength(); cc++) {
							if (param.info_lov_cvalues.item(cc).getNodeName().equals("cvalue")) {
								Node cvalue_node = param.info_lov_cvalues.item(cc);
								NodeList cvalue_kids = cvalue_node.getChildNodes();
								
								int column_count=0;
								int ii=0;
								String tempstr = "";
								for (ii=0; ii < cvalue_kids.getLength(); ii++) {
									if (cvalue_kids.item(ii).getNodeName().equals("column")) {
										Node column_node = cvalue_kids.item(ii);
										column_count++;
										if (column_count == 1)
											tempstr = column_node.getTextContent();
										else
											tempstr += (" - " + column_node.getTextContent());
									}
								}
								
								//Remember parameter=6 of doc_id=19576 which is /FIN/FIN_C2HERPS/FIN-GL-0013b Expenditure Transaction Detail
								//the cvalue after 0159 Leasehold Conversion Program there is only 1 column value when everyone else has 2 column values
								//we are going to make it possible to add "" to templist to make array size good.
								
								if (column_count < cvalue_elem_len) {
									tempstr += " ";
									while (column_count < cvalue_elem_len) {
										tempstr += "- ";
										column_count++;
									}
								}
								
								templist.add(tempstr);
							}
						}
						list_param_choices[pp] = templist.toArray(new String[templist.size()]);
					}
					else if (param.info_values != null) { //info_values
						
						list_param_labels[pp] = param.info_lov_columns_textname.get(0);
						list_param_choices[pp] = param.info_values.toArray(new String[param.info_values.size()]);
						
					}//info_values
				}//lov
			}//Numeric or Text
		}//for pp
		
		list_params_get_choices = 0;
	}//Get_All_List_Param_Choices
	
	
	//*******************************************************************************************************************************
	//list_param_get_choices:
	// 0 = success, found the list of string values for this intervalID of this paramID
	//-1 = found this list of string values for this intervalID of this paramID but the start and end values are not matching
	//-2 = could not find the list of string values for this intervalID of this paramID this code needs to be stepped thru
	//-3 = responseCode that is not 200 from rest-api call
	//-4 = caught a throwable exception need to check crashruncheck text file to debug
	//*******************************************************************************************************************************
	private int Call_And_Parse_LOV_interval_value_elements(String parameter_id, String interval_id, String start_value, String end_value, ArrayList<String> values) {
		
		String debugstr = String.format("Call_And_Parse_LOV_interval_value_elements: docidstr=%s, parameter_id=%s, interval_id=%s, start_value=%s, end_value=%s", docidstr, parameter_id, interval_id, start_value, end_value);
		crashruncheck.println(debugstr);
		
		RestAPIResponse response = my_rest_caller.Get_LOV_Interval(docidstr, parameter_id, interval_id);
		if (response == null)
			return -4; //-4 = caught a throwable exception need to check crashruncheck text file to debug
		else if (response.responseCode != 200)
			return -3; //-3 = responseCode that is not 200 from rest-api call
		
		XML_helper xmlhelp = new XML_helper(crashruncheck);
		xmlhelp.parse(response.response);
		NodeList lov_kids = xmlhelp.parse_LOV_Interval(parameter_id, interval_id);
		if (lov_kids != null) {
			for (int lov=0; lov < lov_kids.getLength(); lov++) {
				if (lov_kids.item(lov).getNodeName().equals("values")) {
					
					ArrayList<String> templist = new ArrayList<String>();
					
					NodeList values_kids       = lov_kids.item(lov).getChildNodes();
					int values_kids_length     = values_kids.getLength();
					for (int vk=0; vk < values_kids_length; vk++) {
						if (values_kids.item(vk).getNodeName().equals("value")) {
							String good_str = values_kids.item(vk).getTextContent();
							templist.add(good_str);
						} 
					}
					
					//return value calculation
					String found_start_value = templist.get(0);
					String found_end_value = templist.get(templist.size() - 1);
					if (start_value.equals(found_start_value) && end_value.equals(found_end_value)) {
						
						for (int tt=0; tt < templist.size(); tt++)
							values.add(templist.get(tt));
						
						return 0; //0 = success, found the list of string values for this intervalID of this paramID
					}
					else
						return -1; //-1 = found this list of string values for this intervalID of this paramID but the start and end values are not matching
				}
			}
		}
		
		return -2; //-2 = could not find the list of string values for this intervalID of this paramID this code needs to be stepped thru
	}//Call_And_Parse_LOV_interval_value_elements
	
	
	private int Call_And_Parse_LOV_interval_CVALUE_elements(String parameter_id, String interval_id, String start_value, String end_value, int map_id, ArrayList<String> values, int max_column_count) {
		
		String debugstr = String.format("Call_And_Parse_LOV_interval_CVALUE_elements: docidstr=%s, parameter_id=%s, interval_id=%s, start_value=%s, end_value=%s, map_id=%d, max_column_count=%d", docidstr, parameter_id, interval_id, start_value, end_value, map_id, max_column_count);
		crashruncheck.println(debugstr);
		
		RestAPIResponse response = my_rest_caller.Get_LOV_Interval(docidstr, parameter_id, interval_id);
		if (response == null)
			return -4; //-4 = caught a throwable exception need to check crashruncheck text file to debug
		else if (response.responseCode != 200)
			return -3; //-3 = responseCode that is not 200 from rest-api call
			
		XML_helper xmlhelp = new XML_helper(crashruncheck);
		xmlhelp.parse(response.response);
		NodeList lov_kids = xmlhelp.parse_LOV_Interval(parameter_id, interval_id);
		if (lov_kids != null) {
			for (int lov=0; lov < lov_kids.getLength(); lov++) {
				if (lov_kids.item(lov).getNodeName().equals("cvalues")) {
					ArrayList<Node> cvalue_nodes = new ArrayList<Node>();
					
					NodeList cvalues_kids   = lov_kids.item(lov).getChildNodes();
					int cvalues_kids_length = cvalues_kids.getLength();
					for (int ck=0; ck < cvalues_kids_length; ck++) {
						if (cvalues_kids.item(ck).getNodeName().equals("cvalue"))
							cvalue_nodes.add(cvalues_kids.item(ck));
					}
					
					//return value calculation
					Node start_node = cvalue_nodes.get(0);
					String[] column_textcontent = new String[max_column_count];
					this.Parse_column_nodes_from_CVALUE_element(start_node, column_textcontent);
					String found_start_value = column_textcontent[map_id];
					
					Node end_node = cvalue_nodes.get(cvalue_nodes.size() - 1);
					column_textcontent = new String[max_column_count];
					this.Parse_column_nodes_from_CVALUE_element(end_node, column_textcontent);
					String found_end_value = column_textcontent[map_id];
					
					if (start_value.equals(found_start_value) && end_value.equals(found_end_value)) {
						
						//get textcontent from all <column> elements
						for (int tt=0; tt < cvalue_nodes.size(); tt++) {
								Node node = cvalue_nodes.get(tt);
								column_textcontent = new String[max_column_count];
								this.Parse_column_nodes_from_CVALUE_element(node, column_textcontent);
								
								//make the choices seen in Parameter_List_Dialog and put into "values" ArrayList<String>
								String val_str = null;
								for (int cc=0; cc < max_column_count; cc++) {
									if (cc==0) {
										if (column_textcontent[0] != null)
											val_str = column_textcontent[0];
										else
											val_str = "";
									}
									else {
										if (column_textcontent[cc] != null)
											val_str += (" - " + column_textcontent[cc]);
										else
											val_str += (" - ");
									}
								}
								values.add(val_str);
						}
						
						
						
						return 0;  //0 = success, found the list of string values for this intervalID of this paramID
					}
					else
						return -1; //-1 = found this list of string values for this intervalID of this paramID but the start and end values are not matching
				}
			}
		}
		
		return -2; //-2 = could not find the list of string values for this intervalID of this paramID this code needs to be stepped thru
	}//Call_And_Parse_LOV_interval_CVALUE_elements
	
	
	//************************************************************************************************************************************
	//Remember parameter=6 of doc_id=19576 which is /FIN/FIN_C2HERPS/FIN-GL-0013b Expenditure Transaction Detail
	//the cvalue after 0159 Leasehold Conversion Program there is only 1 column value when everyone else has 2 column values
	//we need to code for this possibility :(
	//
	//If a column_id is missing then column_textcontent for that index will be null .....
	//
	//https://www.geeksforgeeks.org/default-array-values-in-java/
	//Default array values in Java
	//If we don’t assign values to array elements, and try to access them, compiler does not produce error as in case of simple variables. Instead it assigns values which aren’t garbage.
	//
	//Below are the default assigned values.
	//
	//boolean : false
	//int : 0
	//double : 0.0
	//String : null
	//User Defined Type : null
	//************************************************************************************************************************************
	private void Parse_column_nodes_from_CVALUE_element(Node cvalue_node, String[] column_textcontent) {
		int column_count = 0;
		NodeList cvalue_children = cvalue_node.getChildNodes();
		for (int vv=0; vv < cvalue_children.getLength(); vv++) {
			if (cvalue_children.item(vv).getNodeName().equals("column")) {
				
				Element temp_elem = (Element) cvalue_children.item(vv);
				
				String temp_column_id  = temp_elem.getAttribute("id");
				int column_id = Integer.parseInt(temp_column_id);
				String temp_column_str = temp_elem.getTextContent();
				
				column_textcontent[column_id] = temp_column_str;
			}
		}
	}//Parse_column_nodes_from_CVALUE_element

}//class
