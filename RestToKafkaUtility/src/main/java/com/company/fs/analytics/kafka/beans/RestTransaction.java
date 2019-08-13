package com.company.fs.analytics.kafka.beans;

import java.io.Serializable; 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The application bean class that encapsulate the objects into a single object.
 * It returns tran_id+tran_desc+tran_ts as String
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class RestTransaction implements Serializable{

   	private static final long serialVersionUID = 4412696193395981242L;
	private String tran_id;
    private String tran_desc;
    private String tran_ts;

    public RestTransaction(String tran_id, String tran_desc, String tran_ts) {
    	super();
		this.tran_id = tran_id;
		this.tran_desc = tran_desc;
		this.tran_ts= tran_ts;
    }

    public String getTran_id() {
		return tran_id;
	}
    public void setTran_id(String tran_id) {
		this.tran_id = tran_id;
	}
    public String getTran_desc() {
		return tran_desc;
	}
	public String getTran_ts() {
		return tran_ts;
	}

	public void setTran_ts(String tran_ts) {
		this.tran_ts = tran_ts;
	}

	public void setTran_desc(String tran_desc) {
		this.tran_desc = tran_desc;
	}

	@Override
	public String toString() {
		return "RestTransaction{"+
					"tran_id=" + tran_id + '\'' +
					", tran_desc=" + tran_desc + '\'' +
					", tran_ts=" + tran_ts + '}';
	}
}
