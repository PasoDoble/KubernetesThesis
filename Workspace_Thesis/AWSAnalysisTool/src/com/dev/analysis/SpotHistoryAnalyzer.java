package com.dev.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringTokenizer;

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
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.amazonaws.services.ec2.model.SpotPrice;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.AmazonWebServiceRequest.*;

//Imports for Apache use for Binomial Distribution math
import org.apache.commons.math3.*;
import org.apache.commons.math3.distribution.*;
import org.apache.commons.*;

public class SpotHistoryAnalyzer {
	private final String newLine = System.getProperty("line.separator");
	
	private int maxBid;
	private double desiredAvailability;
	private AmazonEC2 ec2;// = new AmazonEC2Client();
	private AWSCredentials credentials;
	private double maxPricePerBaseline;
	
	private int dictionarySize;
	private HashMap<String, InstanceTypeDefinition> instanceDictionary;
	//private HashMap<String, InstanceTypeDefinition> baselinedInstanceDictionary;
	private ArrayList<Filter> filters;
	private String filterName;
	private ArrayList<String> filterValues;
	
	private ArrayList<File> separatedDataFiles;
	private ArrayList<File> baselinedDataFiles;
	private ArrayList<File> talliedBaselinedDF;
	private ArrayList<File> baselinedCDF;
	private ArrayList<File> cdfFiles;
	private ArrayList<File> talliedRawDF;
	
	private SimpleDateFormat sdf;
	private Date date;
	private String baseDirectory;
	private String startTime;
	private String endTime;
	
	private double baseSpeed;
	private double baseMem;
	private int baseCores;
	private int desiredState;
	private int numberInstanceTypes;
	
	private int[] talliedPrices;
	private int totalTally;
	private int[] cdfValues;
	
	private double lastAvail;
	private int lastNumServers;
	private double lastCost;
	
	//private double[] onDemandBaselinedCosts;
	//private String[] onDemandLabels;
	
	
	public SpotHistoryAnalyzer(AWSCredentials cred, double ppb){
		credentials = cred;
		maxPricePerBaseline = ppb;
		this.init();
	}
	
	public SpotHistoryAnalyzer(AWSCredentials cred, int mb, double av){
		credentials = cred;
		maxBid = mb;
		desiredAvailability = av;
		this.init();
	}
	
	public void init(){
		dictionarySize = 0;
		instanceDictionary = new HashMap<String, InstanceTypeDefinition>();
		filters = new ArrayList<Filter>();
		filterName = "";
		filterValues = new ArrayList<String>();
		
		separatedDataFiles = new ArrayList<File>();
		baselinedDataFiles = new ArrayList<File>();
		talliedBaselinedDF = new ArrayList<File>();
		baselinedCDF = new ArrayList<File>();
		cdfFiles = new ArrayList<File>();
		talliedRawDF = new ArrayList<File>();
		
		sdf = new SimpleDateFormat("yyyyMMdd.HHmmss");
		date = new Date();
		baseDirectory = "e:\\Graduate School\\Thesis\\workspace\\AWSAnalysisTool\\";
		talliedPrices = new int[300000]; //goes to 000.000 or 00.0000 digits
		cdfValues = new int[300000];
		totalTally = 0;
		
		startTime ="";
		endTime = "";
		
		lastAvail = 0.0;
		lastNumServers = 0;
		lastCost = 0.0;
		
		ec2 = new AmazonEC2Client(credentials);
		this.setupDictionary();
		
		//uses default values instead of requesting values
		this.setBaselineTest();
		//this.setBaseline();
		
		this.setupDictionaryWithBaseline();
		
	}
	
	public void setupDictionary(){
		//General use
		
		int newID = 0;
		//T2 good for burstable things, not stable things. Keep note
		//instanceDictionary.put("t2.nano", new InstanceTypeDefinition("t2.nano", 3.3, 1, .5, 0, .0065, newID++));
		//instanceDictionary.put("t2.micro", new InstanceTypeDefinition("t2.micro", 3.3, 1, 1.0, 0, .013, newID++));
		//instanceDictionary.put("t2.small", new InstanceTypeDefinition("t2.small", 3.3, 1, 2.0, 0, .026, newID++));
		//instanceDictionary.put("t2.medium", new InstanceTypeDefinition("t2.medium", 3.3, 2, 4.0, 0, .052, newID++));
		//instanceDictionary.put("t2.large", new InstanceTypeDefinition("t2.large", 3.0, 2, 8.0, 0, .104, newID++));
		
		instanceDictionary.put("m4.large", new InstanceTypeDefinition("m4.large", 2.4, 2, 8.0, 1, .12, newID++));
		instanceDictionary.put("m4.xlarge", new InstanceTypeDefinition("m4.xlarge", 2.4, 4, 16.0, 2, .239, newID++));
		instanceDictionary.put("m4.2xlarge", new InstanceTypeDefinition("m4.2xlarge", 2.4, 8, 32.0, 2, .479, newID++));
		instanceDictionary.put("m4.4xlarge", new InstanceTypeDefinition("m4.4xlarge", 2.4, 16, 64.0, 2, .958, newID++));
		instanceDictionary.put("m4.10xlarge", new InstanceTypeDefinition("m4.10xlarge", 2.4, 40, 160.0, 3, 2.394, newID++));
		
		instanceDictionary.put("m3.medium", new InstanceTypeDefinition("m3.medium", 2.5, 1, 3.75, 1, .067, newID++));
		instanceDictionary.put("m3.large", new InstanceTypeDefinition("m3.large", 2.5, 2, 7.5, 1, .133, newID++));
		instanceDictionary.put("m3.xlarge", new InstanceTypeDefinition("m3.xlarge", 2.5, 4, 15.0, 2, .266, newID++));
		instanceDictionary.put("m3.2xlarge", new InstanceTypeDefinition("m3.2xlarge", 2.5, 8, 30.0, 2, .532, newID++));
		
		//compute optimized
		instanceDictionary.put("c4.large", new InstanceTypeDefinition("c4.large", 2.9, 2, 3.75, 1, .105, newID++));
		instanceDictionary.put("c4.xlarge", new InstanceTypeDefinition("c4.xlarge", 2.9, 4, 7.5, 2, .209, newID++));
		instanceDictionary.put("c4.2xlarge", new InstanceTypeDefinition("c4.2xlarge", 2.9, 8, 15.0, 2, .419, newID++));
		instanceDictionary.put("c4.4xlarge", new InstanceTypeDefinition("c4.4xlarge", 2.9, 16, 30.0, 2, .838, newID++));
		instanceDictionary.put("c4.8xlarge", new InstanceTypeDefinition("c4.8xlarge", 2.9, 36, 60.0, 3, 1.675, newID++));
		
		instanceDictionary.put("c3.large", new InstanceTypeDefinition("c3.large", 2.8, 2, 3.75, 1, .105, newID++));
		instanceDictionary.put("c3.xlarge", new InstanceTypeDefinition("c3.xlarge", 2.8, 4, 7.5, 1, .21, newID++));
		instanceDictionary.put("c3.2xlarge", new InstanceTypeDefinition("c3.2xlarge", 2.8, 8, 15.0, 2, .42, newID++));
		instanceDictionary.put("c3.4xlarge", new InstanceTypeDefinition("c3.4xlarge", 2.8, 16, 30.0, 2, .84, newID++));
		instanceDictionary.put("c3.8xlarge", new InstanceTypeDefinition("c3.8xlarge", 2.8, 32, 60.0, 3, .168, newID++));
		
		//memory optimized, low cost per GB RAM
		instanceDictionary.put("r3.large", new InstanceTypeDefinition("r3.large", 2.5, 2, 15.25, 1, .166, newID++));
		instanceDictionary.put("r3.xlarge", new InstanceTypeDefinition("r3.xlarge", 2.5, 4, 30.5, 1, .333, newID++));
		instanceDictionary.put("r3.2xlarge", new InstanceTypeDefinition("r3.2xlarge", 2.5, 8, 61, 2, .665, newID++));
		instanceDictionary.put("r3.4xlarge", new InstanceTypeDefinition("r3.4xlarge", 2.5, 16, 122, 2, 1.33, newID++));
		instanceDictionary.put("r3.8xlarge", new InstanceTypeDefinition("r3.8xlarge", 2.5, 32, 244, 3, 2.66, newID++));
		
		numberInstanceTypes = newID;
		
		//NOT INCLUDING G2, I2, D2 Instances that focus on graphics and storage
	}
	
	public void setupDictionaryWithBaseline(){
		Iterator it = instanceDictionary.entrySet().iterator();
	    while (it.hasNext()) {
	    	Map.Entry<String, InstanceTypeDefinition> pair = (Map.Entry)it.next();
	    	
	    	InstanceTypeDefinition thisInstance = instanceDictionary.get(pair.getValue().getLabel());
			int thisCores = thisInstance.getCores();
			double thisSpeed = thisInstance.getSpeed();
			double thisMem = thisInstance.getMem();
			
			//double coreRatio = ((double)thisCores)/((double)baseCores);
			//double speedRatio = thisSpeed/baseSpeed;
			double cpuRatio = (((double)thisCores)*thisSpeed)/(((double)baseCores)*baseSpeed); //Judge the speed by the total computing speed
			double memRatio = thisMem/baseMem;
			
			int ratioOfBaselines;
			
			if(cpuRatio < memRatio){
				ratioOfBaselines = (int)cpuRatio;
			}
			else{
				ratioOfBaselines = (int)memRatio;
			}
	    	
	        
	        instanceDictionary.get(pair.getValue().getLabel()).setBaselineFactor(ratioOfBaselines);
	        dictionarySize++;
	        
	    }
	}
	
	//Setup baseline values for one instance of web service
	//Requests user to enter baseline values for each baseline value
	public void setBaseline(){
		Scanner scan = new Scanner(System.in);
		System.out.println("Please enter the processor speed needed (in GHz) per instance: ");
		baseSpeed = scan.nextDouble();
		
		System.out.println("Please enter the number of cores needed per instance: ");
		baseCores = scan.nextInt();
		
		System.out.println("Please enter the amount of RAM needed (in GB) per instance: ");
		baseMem = scan.nextDouble();
		
		System.out.println("Please enter the number of instances you wish to maintain: ");
		desiredState = scan.nextInt();
		
		scan.close();
	}
	
	//Sets a standard baseline in the case that you do not want to be asked for baseline values at start
	public void setBaselineTest(){
		//Baseline for maybe 1000 users per baseline
		baseSpeed = 2;
		baseCores = 1;
		baseMem = 1;
		desiredState = 250;
	}
	
	//Lists all Instance types in order of how many baseline instances they provide
	public InstanceTypeDefinition[] listInstanceTypesByBaselineAmount(){
		InstanceTypeDefinition[] instanceArray = new InstanceTypeDefinition[dictionarySize];
		int instanceCount = 0;
		Iterator it = instanceDictionary.entrySet().iterator();
	    while (it.hasNext()) {
	    	Map.Entry<String, InstanceTypeDefinition> pair = (Map.Entry)it.next();
	    	instanceArray[instanceCount] = pair.getValue();
	        instanceCount++;
	    }
	    InstanceListMergeSort theSortening = new InstanceListMergeSort();
	    instanceArray = theSortening.sort(instanceArray, 3);
	    return instanceArray;
	}
	
	//obtains AWS Spot History Data from Amazon and sends it to the proper files for analysis later
	public void analyzeHistory() throws IOException{
		String nextToken = "";
		filterValues.clear();
		filterValues.add("us-east-1a");
		//filterValues.add("us-east-1b");
		//filterValues.add("us-east-1c");
		//filterValues.add("us-east-1d");
		//filterValues.add("us-east-1e");
		filters.add(new Filter("availability-zone", filterValues));
		
		filterValues.clear();
		/*filterValues.add("t2.nano");
		filterValues.add("t2.micro");
		filterValues.add("t2.small");
		filterValues.add("t2.medium");
		filterValues.add("t2.large");
		filterValues.add("m3.medium");
		filterValues.add("m3.medium");
		filterValues.add("m3.medium");
		filterValues.add("m3.medium");
		filterValues.add("m3.medium");
		filterValues.add("m3.medium");
		filterValues.add("m3.medium");*/
		Iterator it = instanceDictionary.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, InstanceTypeDefinition> pair = (Map.Entry)it.next();
			filterValues.add(pair.getValue().getLabel());
		}
		filters.add(new Filter("instance-type", filterValues));
		
		filterValues.clear();
		filterValues.add("Linux/UNIX");
		filters.add(new Filter("product-description", filterValues));
		
		String directory = baseDirectory+"HistoryData";
		
		//FileWriter f0 = new FileWriter("RawAWSHistory."+sdf.format(date)+".txt"); //DEFINITELY WORKS LINE
		String today = sdf.format(date);
		File rawData = new File(directory,"RawAWSHistory."+today+".txt");
		FileWriter f0 = new FileWriter(rawData);
		
		DescribeSpotPriceHistoryRequest request = new DescribeSpotPriceHistoryRequest().withFilters(filters);
		
		int count = 0;
		SpotPrice current = new SpotPrice();
		do {
			request = request.withNextToken(nextToken);
			
		    //DescribeSpotPriceHistoryRequest request = new DescribeSpotPriceHistoryRequest().withNextToken(nextToken); //DEFINITELY WORKS VERSION

		    // Perform request
		    DescribeSpotPriceHistoryResult result = ec2.describeSpotPriceHistory(request);
		    /*for (int i = 0; i < result.getSpotPriceHistory().size(); i++) {  //DEFINITELY WORKING FOR LOOP TO CONSOLE
		        System.out.println(result.getSpotPriceHistory().get(i));
		    }*/
		    /*for (int i = 0; i < result.getSpotPriceHistory().size(); i++) {  //WORKS PERFECTLY FOR FILE OUTPUT
		    	f0.write(result.getSpotPriceHistory().get(i).toString()+newLine);
		    }*/

		    for (int i = 0; i < result.getSpotPriceHistory().size(); i++) {  //WORKS PERFECTLY FOR FILE OUTPUT
		    	current = result.getSpotPriceHistory().get(i);
		    	f0.write(current.getInstanceType()+","+current.getSpotPrice()+","+current.getTimestamp()+newLine); //REMOVED +current.getAvailabilityZone() BECAUSE I DON'T CARE ABOUT LOCATION YET
		    }
		    System.out.println("Token counter: "+ count++);

		    // 'nextToken' is the string marking the next set of results returned (if any), 
		    // it will be empty if there are no more results to be returned.            
		    nextToken = result.getNextToken();

		} while (!nextToken.isEmpty());
		f0.close();
		System.out.println("AWS Spot History Obtained");
		
		//overwrite any existing file with this name
		CopyOption[] options = new CopyOption[]{
			      StandardCopyOption.REPLACE_EXISTING,
			      StandardCopyOption.COPY_ATTRIBUTES
			    }; 
		
		
		//Copy old raw data into backup
		Path oldRaw = Paths.get(baseDirectory+"RawAWSData.txt");
		Path backupRaw = Paths.get(baseDirectory+"AWSRawBackups\\"+"RawAWSHistory.Replaced"+today+".txt");
		//long cp = Files.copy(new FileInputStream(new File(baseDirectory+"RawAWSData")),new FileOutputStream(new File(baseDirectory+"RawAWSData.+todaysDate"+".txt")));   FAILED ATTEMPT
		Files.copy(oldRaw, backupRaw, options);
		
		//Copy new raw data into common area
		Path newRaw = Paths.get(rawData.getAbsolutePath());
		Files.copy(newRaw, oldRaw, options);
	}
	
	public void createFileDictionary(){
		this.separatedDataFiles.clear();
		File directory = new File(baseDirectory+"HistoryData");
		for(final File file : directory.listFiles()){
			//System.out.println(file.getName());
			if((file.getName()).endsWith(".txt")) separatedDataFiles.add(file);
		}
	}
	
	public void createBaselinedFileDictionary(){
		this.baselinedDataFiles.clear();
		File directory = new File(baseDirectory+"HistoryDataBaselined");
		for(final File file : directory.listFiles()){
			//System.out.println(file.getName());
			if((file.getName()).endsWith(".txt")) this.baselinedDataFiles.add(file);
		}
	}
	
	public void createTalliedBaselinedFileDictionary(){
		this.talliedBaselinedDF.clear();
		File directory = new File(baseDirectory+"HistoryDataTalliedBaselinedCosts");
		for(final File file : directory.listFiles()){
			//System.out.println(file.getName());
			if((file.getName()).endsWith(".txt")) this.talliedBaselinedDF.add(file);
		}
	}
	
	public void createTalliedFileDictionary(){
		this.talliedRawDF.clear();
		File directory = new File(baseDirectory+"HistoryDataTalliedCosts");
		for(final File file : directory.listFiles()){
			//System.out.println(file.getName());
			if((file.getName()).endsWith(".txt")) this.talliedRawDF.add(file);
		}
	}
	
	public void createBaselinedCDFFileDictionary(){
		this.baselinedCDF.clear();
		File directory = new File(baseDirectory+"HistoryDataCDFBaselined");
		for(final File file : directory.listFiles()){
			//System.out.println(file.getName());
			if((file.getName()).endsWith(".txt")) this.baselinedCDF.add(file);
		}
	}
	
	public void createCDFFileDictionary(){
		this.cdfFiles.clear();
		File directory = new File(baseDirectory+"HistoryDataCDF");
		for(final File file : directory.listFiles()){
			//System.out.println(file.getName());
			if((file.getName()).endsWith(".txt")) this.cdfFiles.add(file);
		}
	}
	
	public void createOrderedIDFileList() throws Exception{
			InstanceTypeDefinition[] instanceArray = new InstanceTypeDefinition[dictionarySize];
			Iterator it = instanceDictionary.entrySet().iterator();

		    while (it.hasNext()) {
		    	Map.Entry<String, InstanceTypeDefinition> pair = (Map.Entry)it.next();
		    	instanceArray[pair.getValue().getID()] = pair.getValue();
		    }
		    
			String fileName = "InstanceOrderedID.txt";
			File idFile = new File(baseDirectory,fileName);
			FileWriter IDFile = new FileWriter(idFile);
			
			for(InstanceTypeDefinition inst : instanceArray){
				IDFile.write(inst.getLabel()+newLine);
			}
			IDFile.close();
	}
	
	//Separates raw AWS History Data into separate files named with the instance type
	public void separateRawData() throws IOException{
		//String todaysDate = sdf.format(date);
		
		//REPLACE WITH DIRECTORY YOU WISH TO USE
		String directory = baseDirectory+"HistoryData";
		
		//FileWriter t2n = new FileWriter(new File(directory,"t2.nano.txt"));
		//FileWriter t2m = new FileWriter(new File(directory,"t2.micro.txt"));
		//FileWriter t2s = new FileWriter(new File(directory,"t2.small.txt"));
		//FileWriter t2me = new FileWriter(new File(directory,"t2.medium.txt"));
		//FileWriter t2l = new FileWriter(new File(directory,"t2.large.txt"));
		FileWriter m3m = new FileWriter(new File(directory,"m3.medium.txt"));
		FileWriter m3l = new FileWriter(new File(directory,"m3.large.txt"));
		FileWriter m3xl = new FileWriter(new File(directory,"m3.xlarge.txt"));
		FileWriter m32xl = new FileWriter(new File(directory,"m3.2xlarge.txt"));
		FileWriter m4l = new FileWriter(new File(directory,"m4.large.txt"));
		FileWriter m4xl = new FileWriter(new File(directory,"m4.xlarge.txt"));
		FileWriter m42xl = new FileWriter(new File(directory,"m4.2xlarge.txt"));
		FileWriter m44xl = new FileWriter(new File(directory,"m4.4xlarge.txt"));
		FileWriter m410xl = new FileWriter(new File(directory,"m4.10xlarge.txt"));
		FileWriter c4l = new FileWriter(new File(directory,"c4.large.txt"));
		FileWriter c4xl = new FileWriter(new File(directory,"c4.xlarge.txt"));
		FileWriter c42xl = new FileWriter(new File(directory,"c4.2xlarge.txt"));
		FileWriter c44xl = new FileWriter(new File(directory,"c4.4xlarge.txt"));
		FileWriter c48xl = new FileWriter(new File(directory,"c4.8xlarge.txt"));
		FileWriter c3l = new FileWriter(new File(directory,"c3.large.txt"));
		FileWriter c3xl = new FileWriter(new File(directory,"c3.xlarge.txt"));
		FileWriter c32xl = new FileWriter(new File(directory,"c3.2xlarge.txt"));
		FileWriter c34xl = new FileWriter(new File(directory,"c3.4xlarge.txt"));
		FileWriter c38xl = new FileWriter(new File(directory,"c3.8xlarge.txt"));
		FileWriter r3l = new FileWriter(new File(directory,"r3.large.txt"));
		FileWriter r3xl = new FileWriter(new File(directory,"r3.xlarge.txt"));
		FileWriter r32xl = new FileWriter(new File(directory,"r3.2xlarge.txt"));
		FileWriter r34xl = new FileWriter(new File(directory,"r3.4xlarge.txt"));
		FileWriter r38xl = new FileWriter(new File(directory,"r3.8xlarge.txt"));
		
		File rawFile = new File("RawAWSData.txt");
		//File rawFile = new File(this.baseDirectory,"RawAWSData.txt"); //Just to make certain it stays in the correct directory
		BufferedReader br = new BufferedReader(new FileReader(rawFile));
		String line;
		int count = 0;
		int bigCount = 0;
		
		String firstDate = "";
		String secondToLastDate = "";
		String lastDate = "";
		
		while((line = br.readLine()) != null){
			
			StringTokenizer st = new StringTokenizer(line, ",");
			
			String label = st.nextToken();
			String thisPrice = st.nextToken();
			String thisDate = st.nextToken();
			
			if(count == 0){
				firstDate = thisDate;
			}
			secondToLastDate = lastDate;
			lastDate = thisDate;
			
			String writtenEntry = thisPrice+","+thisDate+newLine;
			switch (label){
			case "t2.nano": //t2n.write(writtenEntry);
							break;
			case "t2.micro": //t2n.write(writtenEntry);
							break;
			case "t2.small": //t2s.write(writtenEntry);
							break;
			case "t2.medium": //t2me.write(writtenEntry);
							break;
			case "t2.large": //t2l.write(writtenEntry);
							break;
			case "m3.medium": m3m.write(writtenEntry);
							break;
			case "m3.large": m3l.write(writtenEntry);
							break;
			case "m3.xlarge": m3xl.write(writtenEntry);
							break;
			case "m3.2xlarge": m32xl.write(writtenEntry);
							break;
			case "m4.large": m4l.write(writtenEntry);
							break;
			case "m4.xlarge": m4xl.write(writtenEntry);
							break;
			case "m4.2xlarge": m42xl.write(writtenEntry);
							break;
			case "m4.4xlarge": m44xl.write(writtenEntry);
							break;
			case "m4.10xlarge": m410xl.write(writtenEntry);
							break;
			case "c4.large": c4l.write(writtenEntry);
							break;
			case "c4.xlarge": c4xl.write(writtenEntry);
							break;
			case "c4.2xlarge": c42xl.write(writtenEntry);
							break;
			case "c4.4xlarge": c44xl.write(writtenEntry);
							break;
			case "c4.8xlarge": c48xl.write(writtenEntry);
							break;
			case "c3.large": c3l.write(writtenEntry);
							break;
			case "c3.xlarge": c3xl.write(writtenEntry);
							break;
			case "c3.2xlarge": c32xl.write(writtenEntry);
							break;
			case "c3.4xlarge": c34xl.write(writtenEntry);
							break;
			case "c3.8xlarge": c38xl.write(writtenEntry);
							break;
			case "r3.large": r3l.write(writtenEntry);
							break;
			case "r3.xlarge": r3xl.write(writtenEntry);
							break;
			case "r3.2xlarge": r32xl.write(writtenEntry);
							break;
			case "r3.4xlarge": r34xl.write(writtenEntry);
							break;
			case "r3.8xlarge": r38xl.write(writtenEntry);
							break;

			}
			
			count++;
			if(count>1000){
				System.out.println("Lines Processed (x1000): "+bigCount++);
				count = 1;
			}
		}
		System.out.println("Raw Data Separation Complete");
		//t2n.close();
		//t2m.close();
		//t2s.close();
		//t2me.close();
		//t2l.close();
		m3m.close();
		m3l.close();
		m3xl.close();
		m32xl.close();
		m4l.close();
		m4xl.close();
		m42xl.close();
		m44xl.close();
		m410xl.close();
		c4l.close();
		c4xl.close();
		c42xl.close();
		c44xl.close();
		c48xl.close();
		c3l.close();
		c3xl.close();
		c32xl.close();
		c34xl.close();
		c38xl.close();
		r3l.close();
		r3xl.close();
		r32xl.close();
		r34xl.close();
		r38xl.close();
		br.close();
		
		//Added for finding the start and end date of all files
		FileWriter beginEnd = new FileWriter(new File(this.baseDirectory,"beginAndEndDates.txt"));
		beginEnd.write(firstDate+newLine);
		beginEnd.write(secondToLastDate+newLine);
		beginEnd.close();
		
		this.createFileDictionary();
		this.considerNullFiles();
	}
	
	public void considerNullFiles() throws IOException{
		for(File file : separatedDataFiles){
			BufferedReader br = new BufferedReader(new FileReader(file));
			if(br.readLine() == null){
				FileWriter theFixer = new FileWriter(file);
				theFixer.write("0,FOREVER");
				theFixer.close();
			}
			br.close();
		}
	}
	
	//needs to be run AFTER the creation of File dictionary
	public void calculatePricePerBaseline() throws IOException{
		this.createFileDictionary();
		for(File file : this.separatedDataFiles){
			
			//Determine the instance type
			String fileName = file.getName();
			int pos = fileName.lastIndexOf(".");
			//if (pos > 0) {   //REMOVING IF STATEMENT BECAUSE ALL .TXT FILES ShOULD BE PRESENT IN THIS DIRECTORY
			//    fileName = fileName.substring(0, pos);
			//}
			String instanceName = fileName.substring(0, pos);
			//System.out.println(fileName);
			
			//Obtain Instance Type Definition, with values of Speed, Cores, and Mem
			InstanceTypeDefinition thisInstance = instanceDictionary.get(instanceName);
			int thisCores = thisInstance.getCores();
			double thisSpeed = thisInstance.getSpeed();
			double thisMem = thisInstance.getMem();
			
			//double coreRatio = ((double)thisCores)/((double)baseCores);
			//double speedRatio = thisSpeed/baseSpeed;
			double cpuRatio = (((double)thisCores)*thisSpeed)/(((double)baseCores)*baseSpeed); //Judge the speed by the total computing speed
			double memRatio = thisMem/baseMem;
			
			int ratioOfBaselines;
			
			/*if(coreRatio < speedRatio && coreRatio < memRatio){
				ratioOfBaselines = (int)coreRatio;
			}
			else if(speedRatio < coreRatio && speedRatio < memRatio){
				ratioOfBaselines = (int)speedRatio;
			}*/
			if(cpuRatio < memRatio){
				ratioOfBaselines = (int)cpuRatio;
			}
			else{
				ratioOfBaselines = (int)memRatio;
			}

			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			String directory = baseDirectory+"HistoryDataBaselined";
			File baselineFile = new File(directory,fileName);
			FileWriter baselinedFile = new FileWriter(baselineFile);


			if(ratioOfBaselines == 0){
				baselinedFile.write("0.0,FOREVER");
			}
			else{
				while((line = br.readLine()) != null){
					StringTokenizer st = new StringTokenizer(line, ",");
					double baselinedCost = Double.parseDouble(st.nextToken());
					baselinedCost = baselinedCost/ratioOfBaselines;
					baselinedFile.write(baselinedCost+","+st.nextToken()+newLine);
				}
			}
			br.close();
			baselinedFile.close();
		}
		System.out.println("Completed baselining costs");
	}

	//NEEDS to run AFTER calculatePricePerBaseline
	public void tallyingTheAbsoluteCosts() throws IOException{
		this.createFileDictionary();
		for(File file : this.separatedDataFiles){

			Arrays.fill(talliedPrices, 0);
			this.totalTally = 0;

			String fileName = file.getName();
			//int pos = fileName.lastIndexOf(".");
			//String instanceName = fileName.substring(0, pos);

			String directory = baseDirectory+"HistoryDataTalliedCosts";
			File theTallyFile = new File(directory,fileName);
			FileWriter talliedFile = new FileWriter(theTallyFile);

			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			int tallyInt = 0;

			while((line = br.readLine()) != null){
				StringTokenizer st = new StringTokenizer(line, ",");
				double baselinedCost = Double.parseDouble(st.nextToken());
				//The cast to int causes the last digits to get removed, meaning this curve should be based off of the right edge of each bucket, right?
				tallyInt = (int)(baselinedCost*10000);                          //MAKE 1000 FOR 000.000 format
				talliedPrices[tallyInt]++;
			}
			br.close();

			int index = 0;
			for(int i : talliedPrices){
				if(i > 0){
					totalTally += i;
					double cost = ((double)index)/10000;
					talliedFile.write(cost+","+i+newLine);
				}
				index++;
			}
			talliedFile.close();
			
			//CDF Work begins
			directory = baseDirectory+"HistoryDataCDF";
			File theCDFDataFile = new File(directory,fileName);
			FileWriter cdfFile = new FileWriter(theCDFDataFile);
			//talliedBaselinedDF.add(cdfFile);
			
			index = 0;
			int tallySoFar = 0;
			for(int i : talliedPrices){
				if(i > 0){
					double cost = ((double)index)/10000;
					tallySoFar += i;
					double percentageAvailable = (((double)tallySoFar)/((double)totalTally))*100;
					cdfFile.write(cost+","+percentageAvailable+newLine);
				}
				index++;
			}
			cdfFile.close();
		}
		System.out.println("Completed tallying the absolute data");
	}

	//NEEDS to run AFTER calculatePricePerBaseline
	public void tallyingTheBaselinedCosts() throws IOException{
		this.createBaselinedFileDictionary();
		//talliedBaselinedDF.clear();
		for(File file : this.baselinedDataFiles){

			Arrays.fill(talliedPrices, 0);
			this.totalTally = 0;

			String fileName = file.getName();
			//int pos = fileName.lastIndexOf(".");
			//String instanceName = fileName.substring(0, pos);

			String directory = baseDirectory+"HistoryDataTalliedBaselinedCosts";
			File theTallyFile = new File(directory,fileName);
			FileWriter talliedFile = new FileWriter(theTallyFile);
			//talliedBaselinedDF.add(theTallyFile);

			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			int tallyInt = 0;

			while((line = br.readLine()) != null){
				StringTokenizer st = new StringTokenizer(line, ",");
				double baselinedCost = Double.parseDouble(st.nextToken());
				//The cast to int causes the last digits to get removed, meaning this curve should be based off of the right edge of each bucket, right?
				tallyInt = (int)(baselinedCost*10000);                          //MAKE 1000 FOR 000.000 format
				talliedPrices[tallyInt]++;
			}
			br.close();

			int index = 0;
			for(int i : talliedPrices){
				if(i > 0){
					totalTally += i;
					double cost = ((double)index)/10000;
					talliedFile.write(cost+","+i+newLine);
				}
				index++;
			}
			talliedFile.close();
			
			//CDF Work begins
			directory = baseDirectory+"HistoryDataCDFBaselined";
			File theCDFDataFile = new File(directory,fileName);
			FileWriter cdfFile = new FileWriter(theCDFDataFile);
			//talliedBaselinedDF.add(cdfFile);
			
			index = 0;
			int tallySoFar = 0;
			for(int i : talliedPrices){
				if(i > 0){
					double cost = ((double)index)/10000;
					tallySoFar += i;
					double percentageAvailable = (((double)tallySoFar)/((double)totalTally))*100;
					cdfFile.write(cost+","+percentageAvailable+newLine);
				}
				index++;
			}
			cdfFile.close();
			
			
		}
		System.out.println("Completed tallying the baselined data");
	}
	
	
	public void tallyingTheBaselinedCostsWithNulls() throws IOException{
		this.createBaselinedFileDictionary();
		//talliedBaselinedDF.clear();
		for(File file : this.baselinedDataFiles){

			Arrays.fill(talliedPrices, 0);

			String fileName = file.getName();
			//int pos = fileName.lastIndexOf(".");
			//String instanceName = fileName.substring(0, pos);

			String directory = baseDirectory+"HistoryDataTalliedBaselinedCosts";
			File theTallyFile = new File(directory,fileName);
			FileWriter talliedFile = new FileWriter(theTallyFile);
			//talliedBaselinedDF.add(theTallyFile);

			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			int tallyInt = 0;

			while((line = br.readLine()) != null){
				StringTokenizer st = new StringTokenizer(line, ",");
				double baselinedCost = Double.parseDouble(st.nextToken());
				//The cast to int causes the last digits to get removed, meaning this curve should be based off of the right edge of each bucket, right?
				tallyInt = (int)(baselinedCost*10000);                          //MAKE 1000 FOR 000.000 format
				talliedPrices[tallyInt]++;
			}
			br.close();
			
			int index = 0;
			for(int i : talliedPrices){
				if(i > 0){
					double cost = ((double)index)/10000;
					talliedFile.write(cost+","+i+newLine);
				}
				index++;
			}
			talliedFile.close();
		}
		System.out.println("Completed tallying the baselined data");
	}
	
	
	
	//************************************ Calculating best cost option lists ****************************************************
	
	
	public InstanceTypeDefinition[] createAvailabilityList() throws IOException{   //Maybe return the list of best options, or the cost expected, etc
		this.createBaselinedCDFFileDictionary();
		
		//InstanceTypeDefinition[] listByAvail = new InstanceTypeDefinition[baselinedCDF.size()];
		Stack<InstanceTypeDefinition> stackInitList = new Stack<InstanceTypeDefinition>();
		//int index = 0;
		
		for(File file : this.baselinedCDF){
			String pdfDir = baseDirectory+"HistoryDataTalliedBaselinedCosts";
			
			String fileName = file.getName();
			int pos = fileName.lastIndexOf(".");
			String instanceName = fileName.substring(0, pos);
			
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			
			double highestCost = 0.0;
			double highestAvail = 0.0;
			
			//int lineCount = 0;
			while((line = br.readLine()) != null){
				//System.out.println("lineCount: "+lineCount+++" file: "+fileName);
				StringTokenizer st = new StringTokenizer(line, ",");
				double baselinedCost = Double.parseDouble(st.nextToken());
				if(baselinedCost > maxPricePerBaseline){
					break;
				}
				highestCost = baselinedCost;
				highestAvail = Double.parseDouble(st.nextToken());
			}
			br.close();
			
			BufferedReader br2 = new BufferedReader(new FileReader(new File(pdfDir, fileName)));
			String line2;
			
			Stack<Double> costs = new Stack<Double>();
			Stack<Integer> tallies = new Stack<Integer>();
			int fullTally = 0;
			while((line2 = br2.readLine()) != null){
				StringTokenizer st = new StringTokenizer(line2, ",");
				costs.push(Double.parseDouble(st.nextToken()));
				int tallier = Integer.parseInt(st.nextToken());
				fullTally += tallier;
				tallies.push(tallier);
			}
			br2.close();
			double avgCost = 0.0;
			while(!costs.empty()){
				avgCost += (costs.pop()*tallies.pop())/fullTally;
			}
			System.out.println("Instance "+instanceName+" has average cost "+avgCost+" highest cost "+highestCost+" and availability "+highestAvail+" and #baslines "+ instanceDictionary.get(instanceName).getBaselineFactor());
			
			if(avgCost > 0){
				stackInitList.add(new InstanceTypeDefinition(instanceName, highestCost, avgCost, highestAvail));
				//listByAvail[index] = new InstanceTypeDefinition(instanceName, highestCost, avgCost, highestAvail);
			}
			//index++;
		}
		InstanceTypeDefinition[] listByAvail = new InstanceTypeDefinition[stackInitList.size()];
		int stackSize = stackInitList.size();
		for(int i = 0; i<stackSize; i++){
			listByAvail[i] = stackInitList.pop();
		}
		return listByAvail;
	}
	
	public InstanceTypeDefinition[] listByBestAvailability() throws IOException{

		InstanceTypeDefinition[] initialList = this.createAvailabilityList();
		InstanceListMergeSort theSorter = new InstanceListMergeSort();
		InstanceTypeDefinition[] listByAvail = theSorter.sort(initialList, 0);
		
		this.writePriorityListFile(listByAvail);
		
		return listByAvail;
	}

	public InstanceTypeDefinition[] listByMinPPB() throws IOException{   //Maybe return the list of best options, or the cost expected, etc

		InstanceTypeDefinition[] initialList = this.createAvailabilityList();
		InstanceListMergeSort theSorter = new InstanceListMergeSort();
		InstanceTypeDefinition[] listByPrice = theSorter.sort(initialList, 1);
		
		this.writePriorityListFile(listByPrice);
		
		return listByPrice;
	}
	
	public InstanceTypeDefinition[] listBySimpleHybrid() throws IOException{   //Maybe return the list of best options, or the cost expected, etc

		InstanceTypeDefinition[] initialList = this.createAvailabilityList();
		InstanceListMergeSort theSorter = new InstanceListMergeSort();
		InstanceTypeDefinition[] listByHybrid = theSorter.sort(initialList, 2);
		
		this.writePriorityListFile(listByHybrid);
		
		return listByHybrid;
	}
	
	public File writePriorityListFile(InstanceTypeDefinition[] input) throws IOException{
		String mainDir = "e:\\Graduate School\\Thesis\\workspace\\AWSAnalysisTool";
		String fileName = "PriorityList.txt";
		File priorityListFile = new File(mainDir, fileName);
		FileWriter listWriter = new FileWriter(priorityListFile);
		
		for(int i = 0; i<input.length; i++){
			InstanceTypeDefinition temp =  input[i];
			
			String typeName = temp.getLabel();
			InstanceTypeDefinition thisInstance =  instanceDictionary.get(typeName);
			
			int thisCores = thisInstance.getCores();
			double thisSpeed = thisInstance.getSpeed();
			double thisMem = thisInstance.getMem();
			
			double cpuRatio = (((double)thisCores)*thisSpeed)/(((double)baseCores)*baseSpeed); //Judge the speed by the total computing speed
			double memRatio = thisMem/baseMem;
			
			int ratioOfBaselines;
			
			if(cpuRatio < memRatio){
				ratioOfBaselines = (int)cpuRatio;
			}
			else{
				ratioOfBaselines = (int)memRatio;
			}
			
			
			listWriter.write(typeName+","+temp.getAvailability()+","+temp.getAverageCost()+","+ratioOfBaselines+newLine); //instance type, avail, cost per base, number of baselines per one obtained
			//System.out.println("Written to file: "+typeName+","+temp.getAvailability()+","+temp.getAverageCost()+","+ratioOfBaselines);
		}
		listWriter.close();
		return priorityListFile;
	}
	
	
	
	
	//*********************************** Math behind obtaining resources and calculating availability ********************************************
	
	
	public void obtainInstances(InstanceTypeDefinition[] input, double targetAvailability) throws IOException{
		String mainDir = baseDirectory+"HistoryThesisLevelGraphs";
		String fileName = desiredState+"ServersAvailability.txt";
		File availabilityChart = new File(mainDir, fileName);
		FileWriter listWriter = new FileWriter(availabilityChart);
		
		//for writing On Demand Prices for graphing
		String nextFileName = desiredState+"ServersAvailabilityOD.txt";
		File availabilityOnDemandChart = new File(mainDir, nextFileName);
		FileWriter listODWriter = new FileWriter(availabilityOnDemandChart);
		int onDemandPercentCount = 0;
		double equivalentOnDemand = 0.0;
		
		InstanceTypeDefinition[] onDemandArray = this.findOnDemandSolution();
		for(InstanceTypeDefinition inst : onDemandArray){
			equivalentOnDemand += inst.getOnDemandPrice();
		}
		
		//create false counts for each server type during TESTING ONLY
		int[] dummyServerCounts = new int[input.length];
		for(int i = 1; i <= input.length; i++){
			//dummyServerCounts[i-1] = i%3;
			dummyServerCounts[i-1] = 2;
		}
		
		boolean moreSpotsExist = true;
		double desiredReliability = targetAvailability;
		int currentTotalServers = 0;  //Meant for storing the number of servers we have, not the number of baselines we can make
		int currentTotalBaselines = 0;
		double currentReliability = 0.0;
		double predictedCostPerHour = 0.0;
		
		ArrayList<InstanceTypeDefinition> obtainedInstances = new ArrayList<InstanceTypeDefinition>();  //Place holder until real cloud instances are obtained and tell us what we have

		//Obtain the initial servers (you can't get 99.99% reliability of 10 servers with 5, so obtain 10 to start
		while(currentTotalBaselines < desiredState && moreSpotsExist){ 
			for(int j = 0; j < dummyServerCounts.length; j++){
				if(dummyServerCounts[j]>0){
					obtainedInstances.add(input[j]);
					currentTotalServers++;
					currentTotalBaselines += instanceDictionary.get(input[j].getLabel()).getBaselineFactor();
					onDemandPercentCount++;
					System.out.println("Obtaining "+input[j].getLabel()+" which is "+instanceDictionary.get(input[j].getLabel()).getBaselineFactor()+" baselines");
					dummyServerCounts[j]--;
					currentReliability = this.calculateAvailability(obtainedInstances, desiredState, currentTotalBaselines);
					predictedCostPerHour = this.calculateExpectedCost(obtainedInstances);
					listWriter.write(currentReliability+","+predictedCostPerHour+newLine);
					//System.out.println("Current Availability: "+currentReliability+"%"+" for a cost of: $"+predictedCostPerHour);
					System.out.println("Current Availability: "+currentReliability+"%"+" for a total number baselines: "+currentTotalBaselines);
					break;
				}
				if((j+1) >= input.length){
					System.out.println("Impossible Request for the currently available spot instances.");
					moreSpotsExist = false;
					break;
				}
			}
			System.out.println("Broke the loop");
			
		}
		
		//****************************** TEST FOR NEW AVAILABILITY CALC *************************************
		
		int desiredServers = currentTotalServers;
		//currentReliability = this.calculateAvailabilityFromNumServers(obtainedInstances, desiredState, currentTotalBaselines, currentTotalServers, desiredServers);
		
		//****************************** END TEST FOR NEW AVAILABILITY CALC *************************************
		
		currentReliability = this.calculateAvailability(obtainedInstances, desiredState, currentTotalBaselines);
		System.out.println("Current Availability: "+currentReliability+"     Desired Availability: "+desiredReliability);
		
		
		while((currentReliability < desiredReliability) && moreSpotsExist){
			//Obtain next available server
			System.out.println("In secondary loop to obtain additional pylons");
			for(int j = 0; j < dummyServerCounts.length; j++){
				if(dummyServerCounts[j]>0){
					obtainedInstances.add(input[j]);
					currentTotalServers++;
					currentTotalBaselines += instanceDictionary.get(input[j].getLabel()).getBaselineFactor();
					onDemandPercentCount++;
					System.out.println("Obtaining "+input[j].getLabel()+" which is "+instanceDictionary.get(input[j].getLabel()).getBaselineFactor()+" baselines");
					dummyServerCounts[j]--;
					
					currentReliability = this.calculateAvailability(obtainedInstances, desiredState, currentTotalBaselines);    
					//********************************************************    TESTING ****************************************
					//currentReliability = this.calculateAvailabilityFromNumServers(obtainedInstances, desiredState, currentTotalBaselines, currentTotalServers, desiredServers);
					
					predictedCostPerHour = this.calculateExpectedCost(obtainedInstances);
					listWriter.write(currentReliability+","+predictedCostPerHour+newLine);
					System.out.println("Current Availability: "+currentReliability+"%"+" for a cost of: $"+predictedCostPerHour);
					break;
				}
				if((j+1) >= input.length){
					System.out.println("Impossible Request for the currently available spot instances.");
					moreSpotsExist = false;
					break;
				}
			}
			System.out.println("Broke the loop");
			
			currentReliability = this.calculateAvailability(obtainedInstances, desiredState, currentTotalBaselines);
			System.out.println("Current Reliability: "+currentReliability+"    Desired Reliability: "+desiredReliability);
			System.out.println("Current Servers: "+currentTotalBaselines);
		}
		System.out.println("Current Reliability: "+currentReliability+"    Desired Reliability: "+desiredReliability+"      END");
		System.out.println("Current Servers: "+currentTotalBaselines);
		
		listWriter.close();
		
		System.out.println("Completed Obtaining Necessary Instances");
		
		predictedCostPerHour = this.calculateExpectedCost(obtainedInstances);
		
		System.out.println("Expected cost per hour with System: $"+predictedCostPerHour);
		
		for(int l = 1; l<=onDemandPercentCount; l++){
			listODWriter.write((100/l)+","+equivalentOnDemand+newLine);
		}
		listODWriter.close();
		
		
		//------------------------- Generate the availability we can guarantee for higher server counts ------------------------------
		
		String newDir = baseDirectory+"HistoryThesisLevelGraphs";
		String newFileName = desiredState+"ServersAvailabilityExcess.txt";
		File excessServerChart = new File(mainDir, newFileName);
		FileWriter excessListWriter = new FileWriter(excessServerChart);
		
		int[] numBaselinesByPercent = this.calculateAvailabilityPerBaselineNumber(obtainedInstances, currentTotalBaselines);
		for(int i = 1; i<100; i++){
			excessListWriter.write(i+".0,"+numBaselinesByPercent[i]+newLine);
		}
		excessListWriter.close();
		
		System.out.println("On Demand Cost Minimum: $"+equivalentOnDemand);
		
		lastAvail = currentReliability;
		this.lastNumServers = currentTotalBaselines;
		lastCost = predictedCostPerHour;
		
		/*
		String percentDir = baseDirectory+"HistoryThesisTenPercentGraphs";
		String percentFileName = desiredState+"ServersAvailability.txt";
		File tenPercentFile = new File(percentDir, percentFileName);
		FileWriter tenPercentWriter = new FileWriter(tenPercentFile);
		
		double[] percents = {90.0, 91.0, 92.0, 93.0, 94.0, 95.0, 96.0, 97.0, 98.0, 99.0, 99.99};
		InstanceTypeDefinition[] listOfThings = this.listBySimpleHybrid();
		for(int i = 0; i < 11; i++){
			obtainInstances(listOfThings, percents[i]);
			tenPercentWriter.write(lastAvail+","+lastNumServers+newLine);
		}
		tenPercentWriter.close();
		
		System.out.println("Final File Written");
		*/
	}
	
	public double calculateAvailability(ArrayList<InstanceTypeDefinition> instances, int newDesiredState, int baselineCount){
		double summedAvailability = 0.0;
		for(InstanceTypeDefinition inst : instances){
			summedAvailability += ((inst.getAvailability()/100.0)*((double)instanceDictionary.get(inst.getLabel()).getBaselineFactor()));
		}
		double approximatedAvailability = summedAvailability/((double)newDesiredState);
		//System.out.println("Current ApproxAvailability: "+approximatedAvailability*100.0+"%    ");
		if(approximatedAvailability > 1){
			approximatedAvailability = 1;
		}
		BinomialDistribution distr = new BinomialDistribution(baselineCount, approximatedAvailability);
		double availability = distr.getProbabilityOfSuccess();
		//System.out.println("Current Availability: "+availability*100.0+"%");
		return availability*100.0;
	}
	
	//Creates a list based on the number of baselines we have obtained to tell us the number of baselines we have available at given percentages
	public int[] calculateAvailabilityPerBaselineNumber(ArrayList<InstanceTypeDefinition> instances, int baselineCount){
		int[] numBaselinesByPercent = new int[100];
		for(int i = 1; i<100; i++){
			//System.out.println("Iteration in baseline availability calculator: "+i);
			double thisPercent = (double)i;
			double currentAvail = 0.0;
			int dState = baselineCount;
			while(currentAvail <= thisPercent){
				currentAvail = this.calculateAvailability(instances, dState, baselineCount);
				dState--;
			}
			numBaselinesByPercent[i] = dState;
		}
		
		return numBaselinesByPercent;
	}
	
	public double calculateAvailabilityFromNumServers(ArrayList<InstanceTypeDefinition> instances, int desiredState, int baselineCount, int serverCount, int desiredServers){
		double summedAvailability = 0.0;
		for(InstanceTypeDefinition inst : instances){
			summedAvailability += inst.getAvailability()/100.0;
		}
		double approximatedAvailability = summedAvailability/((double)desiredServers);
		if(approximatedAvailability > 1){
			approximatedAvailability = 1;
		}
		BinomialDistribution distr = new BinomialDistribution(serverCount, approximatedAvailability);
		double availability = distr.getProbabilityOfSuccess();
		System.out.println("Current Availability: "+availability*100.0+"%    for new calculation method");
		return availability*100.0;
	}
	
	
	public double calculateExpectedCost(ArrayList<InstanceTypeDefinition> instances){
		double predictedCostPerHour = 0.0;
		for(InstanceTypeDefinition instance : instances){
			predictedCostPerHour += (instance.getAverageCost()*instanceDictionary.get(instance.getLabel()).getBaselineFactor());
		}
		return predictedCostPerHour;
	}
	
	
	
	public InstanceTypeDefinition[] findOnDemandSolution(){
		ArrayList<InstanceTypeDefinition> tempInstanceList = new ArrayList<InstanceTypeDefinition>();
		InstanceTypeDefinition[] sortedArray = this.listInstanceTypesByBaselineAmount();
		//for(int l = 0; l< sortedArray.length; l++){
		//	System.out.println(sortedArray[l].getLabel()+" has Baselines: "+sortedArray[l].getBaselineFactor());
		//}
		int numBaselines = 0;
		boolean ranOutOfOptions = false;
		while(numBaselines<desiredState){
			//System.out.println("Need More Vespeen Gas");   //SHOWED INFINITE LOOP @ 6:22 4/26/2106
			int serversRequired = desiredState-numBaselines;
			for(int i = 0; i<sortedArray.length; i++){
				//System.out.println("In For 1");
				if(sortedArray[i].getBaselineFactor() <= serversRequired){
					//System.out.println("In If 1");
					tempInstanceList.add(sortedArray[i]);
					numBaselines += sortedArray[i].getBaselineFactor();
					break;
				}
				else if((i+1) >= sortedArray.length){
					System.out.println("In If 2");
					for(int j = 0; j<sortedArray.length; j++){
						//System.out.println("In For 2");
						if(sortedArray[j].getBaselineFactor() == 1 || j+1 > sortedArray.length || (j+1 <= sortedArray.length && sortedArray[j+1].getBaselineFactor() < 1)){
							tempInstanceList.add(sortedArray[j]);
							numBaselines += sortedArray[j].getBaselineFactor();
							break;
						}
					}
					break;
				}
			}
		}
		
		InstanceTypeDefinition[] instanceList = new InstanceTypeDefinition[tempInstanceList.size()];
		int k = 0;
		for(InstanceTypeDefinition inst : tempInstanceList){
			instanceList[k] = inst;
			k++;
		}
		return instanceList;
	}
	
	
	
	public void obtainInstancesLevels() throws IOException{
		String newDir = baseDirectory+"HistoryThesisTenPercentGraphs";
		String newFileName = desiredState+"ServersAvailability.txt";
		String newPriceFileName = desiredState+"ServersPricing.txt";
		File tenPercentFile = new File(newDir, newFileName);
		FileWriter tenPercentWriter = new FileWriter(tenPercentFile);
		
		File tenPercentPriceFile = new File(newDir, newPriceFileName);
		FileWriter tenPercentPriceWriter = new FileWriter(tenPercentPriceFile);
		
		double[] percents = {90.0, 91.0, 92.0, 93.0, 94.0, 95.0, 96.0, 97.0, 98.0, 99.0, 99.99};
		InstanceTypeDefinition[] listOfThings = this.listBySimpleHybrid();
		for(int i = 0; i < 11; i++){
			obtainInstances(listOfThings, percents[i]);
			//tenPercentWriter.write(lastAvail+","+lastNumServers+newLine);
			tenPercentWriter.write(percents[i]+","+lastNumServers+newLine);
			tenPercentPriceWriter.write(percents[i]+","+lastCost+newLine);
		}
		tenPercentWriter.close();
		tenPercentPriceWriter.close();
	}
	
	//************************************************ Testing for Instance Type Independence ***********************************************
	
	//Failed attempt at determining independence without a proper correlation function
	//Create the interdependence map of all instance types
	/*public double[][] determineInstanceIndependence(boolean[][] allInstancesMap) throws Exception{
		String newDir = baseDirectory+"InterAvailabilities\\BidLevel_$"+maxPricePerBaseline;
		File bidLevelDirectory = new File(newDir);
		
		bidLevelDirectory.mkdirs();
		
		File instanceMapping = new File(newDir, "PercentageDependanceMapping.txt");
		FileWriter mapWriter = new FileWriter(instanceMapping);
		
		double[][] interAvailabilityMap = new double[numberInstanceTypes][numberInstanceTypes];
		int[][] interAvailabilityCounter = new int[numberInstanceTypes][numberInstanceTypes];
		int numberTimeIndices = allInstancesMap[0].length;
		
		double[] instancePercentTimeAvailable = new double[numberInstanceTypes];
		int[] instancePercentTimeCount = new int[numberInstanceTypes];
		
		for(int instanceIDMain = 0; instanceIDMain < numberInstanceTypes; instanceIDMain++){
			
			// EFFICIENT SOLUTION, NOT PERFECT FOR FILE STORAGE    for(int instanceIDSub = instanceIDMain+1; instanceIDSub < numberInstanceTypes; instanceIDSub++){
			for(int instanceIDSub = 0; instanceIDSub < numberInstanceTypes; instanceIDSub++){
				
				for(int timeIndex = 0; timeIndex < numberTimeIndices; timeIndex++){
					if(allInstancesMap[instanceIDMain][timeIndex] == allInstancesMap[instanceIDSub][timeIndex]){  //Only counting trues: if(allInstancesMap[instanceIDMain][timeIndex] && allInstancesMap[instanceIDSub][timeIndex]){
						interAvailabilityCounter[instanceIDMain][instanceIDSub]++;
					}
					if(allInstancesMap[instanceIDMain][timeIndex]) instancePercentTimeCount[instanceIDMain]++;
				}
				interAvailabilityMap[instanceIDMain][instanceIDSub] = ((double)interAvailabilityCounter[instanceIDMain][instanceIDSub])/((double)allInstancesMap[instanceIDMain].length);
				mapWriter.write(interAvailabilityMap[instanceIDMain][instanceIDSub]*100.0+",");  //multiply 100 for percentage
				
			}
			mapWriter.write(newLine);
			System.out.println("All availabilities done for ID: "+instanceIDMain);
			
			// IN CASE YOU EVER CARE WHAT PERCENT OF TIME YOU HAVE THIS INSTANCE AVAILABLE
			instancePercentTimeAvailable[instanceIDMain] = ((double)instancePercentTimeCount[instanceIDMain])/((double)allInstancesMap[instanceIDMain].length);
		}
		mapWriter.close();
		return interAvailabilityMap;
	}*/
	
	//Create the array and file for the dependence of one file on all other files
	public double[] determineSingleInstanceDependence(){
		return null;
	}
	
	//Maps the yes available/no available times for an instance type
	public boolean[] createInstanceTimeMap(File file, File directory, long timeFrameMinutes, long theEndTimeMillis) throws Exception{
		int numberIndices = ((int)timeFrameMinutes/10) + 1;
		boolean[] instanceTimeMap = new boolean[numberIndices];
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		
		String baselinedFileName = file.getName();
		FileWriter interAvailabilityWriter = new FileWriter(new File(directory, baselinedFileName)); //Double check the file.getName is what I want to use here
		
		while((line = br.readLine()) != null){
			StringTokenizer st = new StringTokenizer(line, ",");
			
			String thisPrice = st.nextToken();
			String thisDate = st.nextToken();
			double thePrice = Double.parseDouble(thisPrice);
			
			if(thisDate.equals("FOREVER")) break; //How I dealt with instances that couldn't be used: set their price to 0 forever
			
			
			if(thePrice <= maxPricePerBaseline){
				DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
				Date lineDate = (Date)formatter.parse(thisDate);
				long lineTimeMillis = lineDate.getTime();
				long relativeMillis = lineTimeMillis - theEndTimeMillis;

				//System.out.println("Line Millis: "+lineTimeMillis);

				int indexOfBoolean = (int)(relativeMillis/600000); //60,000 = conversion of milliseconds to minutes, 10 for conversion of indices of 10 minute intervals
				//System.out.println(indexOfBoolean);
				instanceTimeMap[indexOfBoolean] = true;
			}

		}
		
		br.close();
		System.out.println("Doing availability mapping for: "+baselinedFileName);
		for(int i = 0; i<numberIndices; i++){
			interAvailabilityWriter.write(i+","+instanceTimeMap[i]+newLine);
		}
		interAvailabilityWriter.close();
		return instanceTimeMap;
	}
	
	//Calls the above for every file
	//Array format 5/3/16: Has an index for every 10 minute interval in the 90 day history
	//Assumptions 5/3/16: The usual spot instance is not lost from any non-bid source in less than 10 minutes
	public boolean[][] createAllInstancesTimeMap() throws Exception{//int timeFrame, int startTime, int endTime) throws IOException{
		this.createBaselinedFileDictionary();
		
		String newDir = baseDirectory+"TimeMapping\\BidLevel_$"+maxPricePerBaseline;
		File bidLevelDirectory = new File(newDir);
		
		bidLevelDirectory.mkdirs();
		
		File beginEnd = new File(this.baseDirectory,"beginAndEndDates.txt");
		BufferedReader br = new BufferedReader(new FileReader(beginEnd));
		
		startTime = br.readLine();
		endTime = br.readLine();
		br.close();
		
		//System.out.println(startTime);
		//System.out.println(endTime);
		
		DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
		Date startDate = (Date)formatter.parse(startTime);
		Date endDate = (Date)formatter.parse(endTime);
		
		//System.out.println(startDate);
		//System.out.println(endDate);
		
		long startTimeMillis = startDate.getTime();
		long endTimeMillis = endDate.getTime();
		
		System.out.println("Millis End: "+endTimeMillis);
		//System.out.println(startTimeMillis);
		
		long relativeStartMillis = startTimeMillis - endTimeMillis;
		System.out.println(relativeStartMillis);
		
		long relativeStartMinutes = relativeStartMillis/60000;
		System.out.println(relativeStartMinutes);
		
		int numberIndices = ((int)relativeStartMinutes/10) + 1;
		boolean[][] InstanceTimeMap = new boolean[numberInstanceTypes][numberIndices];
		
		for(File baselinedFile : baselinedDataFiles){
			String baselinedFileName = baselinedFile.getName();
			int pos = baselinedFileName.lastIndexOf(".");
			String instanceType = baselinedFileName.substring(0, pos);
			
			int instanceID = instanceDictionary.get(instanceType).getID();
			
			InstanceTimeMap[instanceID] = createInstanceTimeMap(baselinedFile, bidLevelDirectory, relativeStartMinutes, endTimeMillis);
			
			
			
		}

		return InstanceTimeMap;
	}


	
	
	
	
	
	
	
	
	
	//////////////////////////////////////////////////////////////////////// Pricing Time Map ////////////////////////////////////////////////////////////////////////////
	
	//Same  as above but stores the price at every time instead of boolean
	public double[][] createAllInstancesPRICINGTimeMap() throws Exception{//int timeFrame, int startTime, int endTime) throws IOException{
		this.createBaselinedFileDictionary();

		String newDir = baseDirectory+"TimeMapping\\BidLevel_$"+maxPricePerBaseline+"_PRICING";
		File bidLevelDirectory = new File(newDir);

		bidLevelDirectory.mkdirs();

		File beginEnd = new File(this.baseDirectory,"beginAndEndDates.txt");
		BufferedReader br = new BufferedReader(new FileReader(beginEnd));

		startTime = br.readLine();
		endTime = br.readLine();
		br.close();

		//System.out.println(startTime);
		//System.out.println(endTime);

		DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
		Date startDate = (Date)formatter.parse(startTime);
		Date endDate = (Date)formatter.parse(endTime);

		//System.out.println(startDate);
		//System.out.println(endDate);

		long startTimeMillis = startDate.getTime();
		long endTimeMillis = endDate.getTime();

		System.out.println("Millis End: "+endTimeMillis);
		//System.out.println(startTimeMillis);

		long relativeStartMillis = startTimeMillis - endTimeMillis;
		System.out.println(relativeStartMillis);

		long relativeStartMinutes = relativeStartMillis/60000;
		System.out.println(relativeStartMinutes);

		int numberIndices = ((int)relativeStartMinutes/10) + 1;
		double[][] InstanceTimeMap = new double[numberInstanceTypes][numberIndices];
		
		
		File instancePriceMapping = new File(newDir, "PriceTimeMapping.txt");
		FileWriter mapPriceWriter = new FileWriter(instancePriceMapping);

		for(File baselinedFile : baselinedDataFiles){
			String baselinedFileName = baselinedFile.getName();
			int pos = baselinedFileName.lastIndexOf(".");
			String instanceType = baselinedFileName.substring(0, pos);

			int instanceID = instanceDictionary.get(instanceType).getID();

			InstanceTimeMap[instanceID] = createInstancePRICINGTimeMap(baselinedFile, bidLevelDirectory, relativeStartMinutes, endTimeMillis);
			
			for(double d : InstanceTimeMap[instanceID]){
				mapPriceWriter.write(d+",");
			}
			mapPriceWriter.write(newLine);
		}
		mapPriceWriter.close();
		System.out.println("Completed Pricing HeatMap Data Generation");
		return InstanceTimeMap;
	}

	public double[] createInstancePRICINGTimeMap(File file, File directory, long timeFrameMinutes, long theEndTimeMillis) throws Exception{
		int numberIndices = ((int)timeFrameMinutes/10) + 1;
		double[] instanceTimeMap = new double[numberIndices];
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		
		String baselinedFileName = file.getName();
		FileWriter interAvailabilityWriter = new FileWriter(new File(directory, baselinedFileName)); //Double check the file.getName is what I want to use here
		
		while((line = br.readLine()) != null){
			StringTokenizer st = new StringTokenizer(line, ",");
			
			String thisPrice = st.nextToken();
			String thisDate = st.nextToken();
			double thePrice = Double.parseDouble(thisPrice);
			
			if(thisDate.equals("FOREVER")) break; //How I dealt with instances that couldn't be used: set their price to 0 forever
			
			
			if(thePrice <= maxPricePerBaseline){
				DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
				Date lineDate = (Date)formatter.parse(thisDate);
				long lineTimeMillis = lineDate.getTime();
				long relativeMillis = lineTimeMillis - theEndTimeMillis;

				//System.out.println("Line Millis: "+lineTimeMillis);

				int indexOfBoolean = (int)(relativeMillis/600000); //60,000 = conversion of milliseconds to minutes, 10 for conversion of indices of 10 minute intervals
				//System.out.println(indexOfBoolean);
				instanceTimeMap[indexOfBoolean] = thePrice;
			}

		}
		
		br.close();
		System.out.println("Doing availability mapping for: "+baselinedFileName);
		for(int i = 0; i<numberIndices; i++){
			interAvailabilityWriter.write(i+","+instanceTimeMap[i]+newLine);
		}
		interAvailabilityWriter.close();
		return instanceTimeMap;
	}

}