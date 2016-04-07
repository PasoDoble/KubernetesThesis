package com.dev.analyzer;

public class InstanceTypeDefinition {
	
	public String label;
	public int speed; //in MHz
	public int cores;
	public int ram; //in MB
	
	public InstanceTypeDefinition(String l, int s, int c, int r){
		label = l;
		speed = s;
		cores = c;
		ram = r;
	}
	
	
}
