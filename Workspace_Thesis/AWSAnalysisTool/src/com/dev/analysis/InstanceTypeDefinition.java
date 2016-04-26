package com.dev.analysis;

public class InstanceTypeDefinition {
	
	public String label;
	public String availabilityZone;
	public double speed; //in GHz
	public int cores;
	public double ram; //in GB
	public int harddrive;
	public int networking; //0=low, 1 = mod, 2 = high, 3= 10 Gigabit
	public double onDemandPrice;
	
	public double highCost;
	public double averageCost;
	public double availability;
	public int baselineFactor;
	
	public InstanceTypeDefinition(String l, double s, int c, double r){
		label = l;
		speed = s;
		cores = c;
		ram = r;
		harddrive = 0;
	}
	
	/*public InstanceTypeDefinition(String l, double s, int c, double r, int h){
		label = l;
		speed = s;
		cores = c;
		ram = r;
		harddrive = h;
	}*/
	
	public InstanceTypeDefinition(String l, double s, int c, double r, int n, double odc){
		label = l;
		speed = s;
		cores = c;
		ram = r;
		networking = n;
		onDemandPrice = odc;
	}
	
	public InstanceTypeDefinition(String l, double h, double avg, double a){
		label = l;
		highCost = h;
		averageCost = avg;
		availability = a;
	}
	
	public String getLabel(){
		return label;
	}
	
	public int getCores(){
		return cores;
	}
	
	public double getMem(){
		return ram;
	}
	
	public double getSpeed(){
		return speed;
	}
	
	public int returnNetworking(){
		return networking;
	}
	
	public double getHighCost(){
		return highCost;
	}
	
	public void setHighCost(double c){
		highCost = c;;
	}
	
	public double getAverageCost(){
		return averageCost;
	}
	
	public void setAverageCost(double avg){
		averageCost = avg;
	}
	
	public double getAvailability(){
		return availability;
	}
	
	public void setAvailability(double a){
		availability = a;
	}
	
	public double getOnDemandPrice(){
		return onDemandPrice;
	}
	
	public void setOnDemandPrice(double newp){
		onDemandPrice = newp;
	}
	
	public int getBaselineFactor(){
		return this.baselineFactor;
	}
	
	public void setBaselineFactor(int newbase){
		this.baselineFactor = newbase;
	}
	
}
