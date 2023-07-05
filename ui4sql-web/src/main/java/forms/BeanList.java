/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package forms;

import java.util.List;

/*
 * used to create a list of WebFields
 * 
 *  may be possible to do this in the xml, but I don't know how.
 *  
 */

public class BeanList {

	private List <BeanWebField >  list;

	public BeanList() {
	}
	
	

	
	public void setList (List <BeanWebField > list) {
		System.out.println("setBeanList");
		this.list = list;
	}
	
	public List <BeanWebField > getList (List <BeanWebField > list) {
		System.out.println("getBeanList ");
		return this.list;
	}
	
	
	
	public void setWebFields(List <BeanWebField > list) {
		System.out.println("setWebFields");
		this.list = list;
	}
	

	

	//public void setWebFields (BeanWebField[] beanArray) {
		//this.beanArray = beanArray;
	//	}
	
	public List <BeanWebField  >  getWebFields() {
		return list;
	}
	
	
	
	
	/*
	 * convert the List to an array
	 */
	public BeanWebField[] getBeanFieldArray() {
		
		//System.out.println("BeanList - getWebFieldArray");
		
		BeanWebField [] beans = new BeanWebField[list.size()];
		for (int x = 0; x < list.size(); x++) {
			beans [x] = (BeanWebField) list.get(x);
			}
		
		//System.out.println("BeanList - returning beans " + beans.length);
		
		
		return beans;
	}

}
