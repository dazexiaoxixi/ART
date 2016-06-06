package com.example.database;

public class SocketConfigInfo {
	private String IpStr;
	private String PortStr;
	public SocketConfigInfo(String IpStr,String PortStr) {
		this.IpStr = IpStr;
		this.PortStr = PortStr;
	}
	public SocketConfigInfo(){
		
	}
	public String getIpStr(){
		return IpStr;
	}
	public String getPortStr(){
		return PortStr;
	}
	public void setIpStr(String IpStr){
		this.IpStr = IpStr;
	}
	public void setPortStr(String PortStr){
		this.PortStr = PortStr;
	}
}
