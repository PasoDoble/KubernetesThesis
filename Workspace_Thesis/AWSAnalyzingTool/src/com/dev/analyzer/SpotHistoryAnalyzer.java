package com.dev.analyzer;

import java.util.HashMap;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CancelSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
import com.amazonaws.services.ec2.model.DescribeSpotPriceHistoryRequest;
import com.amazonaws.services.ec2.model.DescribeSpotPriceHistoryResult;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.AmazonWebServiceRequest.*;

public class SpotHistoryAnalyzer {
	private int maxBid;
	private double desiredAvailability;
	private AmazonEC2 ec2;// = new AmazonEC2Client();
	private AWSCredentials credentials;
	
	private HashMap<String, InstanceTypeDefinition> instanceDictionary;
	
	public SpotHistoryAnalyzer(AWSCredentials cred){
		credentials = cred;
		ec2 = new AmazonEC2Client(credentials);
	}
	
	public SpotHistoryAnalyzer(AWSCredentials cred, int mb, double av){
		credentials = cred;
		maxBid = mb;
		desiredAvailability = av;
		ec2 = new AmazonEC2Client(credentials);
	}
	
	public void analyzeHistory(){
		String nextToken = "";
		do {
		    // Prepare request (include nextToken if available from previous result)
		    DescribeSpotPriceHistoryRequest request = new DescribeSpotPriceHistoryRequest()
		            .withNextToken(nextToken);

		    // Perform request
		    DescribeSpotPriceHistoryResult result = ec2
		            .describeSpotPriceHistory(request);
		    for (int i = 0; i < result.getSpotPriceHistory().size(); i++) {
		        System.out.println(result.getSpotPriceHistory().get(i));
		    }

		    // 'nextToken' is the string marking the next set of results returned (if any), 
		    // it will be empty if there are no more results to be returned.            
		    nextToken = result.getNextToken();

		} while (!nextToken.isEmpty());
	}
	
}