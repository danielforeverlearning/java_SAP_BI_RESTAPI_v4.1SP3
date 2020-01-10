

//*************************************************************************************************************************************
//Ok did testing on doc_id=592369 which is really /Development/Maxwell/maxtest_FIN-DEBT-HON0030 Debt Registry Balances by Series
//And then i thought if lov, then it will have either info_lov_cvalues or info_lov_intervals or info_lov_values
//And if NOT lov, then it may have info_values such as in single select DateTime
//
//But .....
//I was wrong because doc_id=19576 which is really /FIN/FIN_C2HERPS/FIN-GL-0013b Expenditure Transaction Detail
//on parameter 6
//it has info_lov_cvalues AND info_lov_intervals
//
//WARNING !!!!! WARNING !!!!!
//(1) for doc_id=19576 on parameter=6 on the cvalue after 0159 Leasehold Conversion Program there is only 1 column value when everyone else has 2 column values
//so i need to make better code to handle things like this ..... previously assumed all will have 2 columns according to XML columns node, but i was wrong.
//
//(2) for doc_id=19576 on parameter=6 intervals contain "cvalue xml-elements" unlike doc_id=592369 parameter=4 which contains "value xml-elements" 
//***************************************************************************************************************************************************************


package sap_bi_restapi_package;

import java.util.ArrayList;
import org.w3c.dom.NodeList;

public class DocumentRefresh_Parameter {
	public String parameter_type;
	public String parameter_optional;
	public String parameter_id;
	public String parameter_name;
	public String answer_type;
	
	public ArrayList<String> answer_values;
	
	public String info_cardinality;
	
	public String info_lov_refreshable;                   //if lov this will be set
	public String info_lov_partial;                       //if lov this will be set
	public String info_lov_hierarchical;                  //if lov this will be set
	
	public ArrayList<String> info_values; 
	
	public ArrayList<String> info_lov_values;
	public NodeList          info_lov_cvalues;             //see WARNING !!!!! above
	
	public String            info_lov_columns_mappingId;   //integer
	public ArrayList<String> info_lov_columns_type;		   //see WARNING !!!!! above
	public ArrayList<String> info_lov_columns_textname;    //see WARNING !!!!! above
	
	public NodeList          info_lov_intervals;           //see WARNING !!!!! above
														   //sometimes contains "value xml-elements" (interval_id_0, start_value, end_value, interval_id_1, start_value, end_value ..... etc)
														   //sometimes contains "cvalue xml-elements" which depends on info_lov_columns_mappingId, info_lov_columns_type, info_lov_columns_textname
														   //we have to query for values per interval_id
	
	
	                                                       //Inside <interval></interval> it can be <cvalue> or <value> for example:
	
	                                                       //Example: <interval><cvalue>.....</cvalue></interval>
	                                                       //So info_lov_cvalues should NOT BE NULL

	                                                       //Example: <interval><value>.....</value></interval>
	                                                       //So info_lov_values should NOT BE NULL
	
	
	
	public boolean is_text_button_parameter() {
		if (answer_type.equals("Text") || answer_type.equals("Numeric")) {
			if ((info_lov_partial == null) || (info_lov_partial != null && info_lov_partial.equals("false"))) {
				if (info_lov_cvalues == null && info_values == null && info_lov_intervals == null) {
						return true;
				}
			}
		}
		
		return false;
	}//is_text_button_parameter
	
	
	
}//class DocumentRefresh_Parameter