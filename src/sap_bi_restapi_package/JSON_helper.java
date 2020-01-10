

package sap_bi_restapi_package;

import java.io.FileReader; 
import java.util.Iterator; 
import java.util.Map; 
  
import org.json.simple.JSONArray; 
import org.json.simple.JSONObject; 
import org.json.simple.parser.*;


//***********************************************************************************
//    JSON tutorial (1):
//    https://stackabuse.com/reading-and-writing-json-in-java/
//    https://stackabuse.com/reading-and-writing-json-in-java/
//
//    JSON is a generic data format that has six data types:
//    Strings, Numbers, Booleans, Arrays, Objects, null
//  
//    Example (1): object containing string "name", number "age", boolean "isMarried", and array "kids" which has 2 objects, each of these objects contains 1 string and 1 number
//    {
//        "name": "Benjamin Watson",
//        "age": 31,
//        "isMarried": true,
//        "hobbies": ["Football", "Swimming"],
//        "kids": [
//        {
//            "name": "Billy",
//            "age": 5
//        },
//        {
//            "name": "Milly",
//            "age": 3
//        }]
//    }
//
//    JSON tutorial (2):
//    http://www.java2s.com/Tutorials/Java/JSON/index.htm
//    http://www.java2s.com/Tutorials/Java/JSON/index.htm
//
//    Example (2): object containing array "book" which has 2 objects, each of these objects contains 4 strings
//    {
//        "book": [
//        {
//            "id":"01",
//            "language": "Java",
//            "edition": "third",
//            "author": "java2s.com"
//        },
//        {
//            "id":"02",
//            "language": "JSON",
//            "edition": "second"
//            "author": "Jack"
//        }]
//    }
//
//    JSON syntax can be summarized as follows:
//
//    Data is represented in name/value pairs.
//    Curly braces hold objects, the name/value pairs are separated by ,.
//    Square brackets hold arrays and values are separated by ,.
//    JSON supports the following two data structures:
//
//    Data structure                         Description
//    -----------------------------------------------------------------------------------------
//    Collection of name/value pairs         key:value,key:value,
//    Ordered list of values (array)	     [1,2,3,4]
//
//
//    The following table lists the data types supported by JSON.
//    Type                                   Description
//    -----------------------------------------------------------------------------------------
//    Number                                 double-precision, floating-point format in JavaScript. Octal and hexadecimal formats are not used. No NaN or Infinity. Example, 1,9, 0,-4. Fractions like .3, .9
//                                           Exponent like e, e+, e-,E, E+, E-
//                                           var json-object-name = { string : number_value}
//
//    String                                 double-quoted Unicode with backslash escaping.
//                                           Escape sequence: \b \f \n \r \t (also slash and u but on javac.exe this gives "error: illegal unicode escape")
//                                           var json-object-name = { string : "string value"}
//
//    Boolean                                true or false
//                                           var json-object-name = { name: true/false,}
//
//    Array                                  An ordered sequence of values.
//                                           Array elements are enclosed square brackets [element,element,element, ].
//
//    Value                                  Can be a string, a number, true or false, null etc
//
//    Object                                 An unordered collection of key:value pairs.
//                                           Object are enclosed in curly braces starts with '{' and ends with '}'.
//                                           key:value pairs are separated by ,
//                                           The keys must be strings and should be different from each other.
//                                           { string : value, string1 : value1,.......}
//
//    Whitespace                             can be used between any pair of tokens
//
//    null                                   empty
//
//
//    JSON tutorial (3):
//    https://www.geeksforgeeks.org/parse-json-java/
//    https://www.geeksforgeeks.org/parse-json-java/
//
//    Example (3): Below is a simple example from Wikipedia that shows JSON representation of an object that describes a person.
//                 The object has string values for first name and last name, a number value for age, an object value representing the person’s address, and an array value of phone number objects.
//
//    {
//        "firstName": "John",
//        "lastName": "Smith",
//        "age": 25,
//        "address": {
//            "streetAddress": "21 2nd Street",
//            "city": "New York",
//            "state": "NY",
//            "postalCode": 10021
//        },
//        "phoneNumbers": [
//            {
//                "type": "home",
//                "number": "212 555-1234"
//            },
//            {
//                "type": "fax",
//                "number": "646 555-4567" 
//            }
//        ] 
//    }
//
//
//    json-simple
//    https://code.google.com/archive/p/json-simple/
//    https://code.google.com/archive/p/json-simple/
//
//
//***********************************************************
public class JSON_helper {
	
	private CrashAndRunChecker crashruncheck;
	
	public JSON_helper(CrashAndRunChecker tempcrashruncheck) {
		crashruncheck = tempcrashruncheck;
	}//JSON_helper
	
	public void DEBUG_Simple_JSON_File_Test(String filenamepath) {
		
		try {
			Object obj = new JSONParser().parse(new FileReader(filenamepath));
				
			// typecasting obj to JSONObject
			JSONObject jo = (JSONObject) obj;
				
			Map testmap = ((Map)jo);
			// iterating testmap 
			Iterator<Map.Entry> itr1 = testmap.entrySet().iterator();
			while (itr1.hasNext()) { 
				Map.Entry pair = itr1.next();
				crashruncheck.println(pair.getKey() + " : " + pair.getValue()); 
		    }
		}
		catch (Throwable tt) {
			crashruncheck.println("DEBUG_Simple_JSON_File_Test", tt);
		}
	}//DEBUG_Simple_JSON_File_Test

}//class
