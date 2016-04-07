package com.dev.analyzer;



public class AvailabilityCalculator {
	private double[] availabilities;
	private double[] expectedCosts;
	private int desiredState; //Desired number of available servers at target availability
	private int numServersAvail; //Number of servers available with different availabilities at a given cost
	private int maxBid; //Current max bid price
	private double desiredAvailability; //desired availability percentage
	
	public AvailabilityCalculator(double[] a, double[] ec, int ds, int sa, int mb, int da){
		availabilities = a;
		expectedCosts = ec;
		desiredState = ds;
		numServersAvail = sa;
		maxBid = mb;
		desiredAvailability = da;
	}
	
	
	
	public double expectedCostPerHour(){
		double cost = 0.0;
		for(Double d : expectedCosts){
			cost += d;
		}
		return cost;
	}
}
