#Changing things up a bit
#install.packages("gplots")
library(gplots)

dir = "e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool\\TimeMapping\\BidLevel_$0.02"
newDir = "e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool\\InterAvailabilities\\BidLevel_$0.02"
baseDir <- "e:\\Graduate\ School\\Thesis\\workspace\\AWSAnalysisTool"

orderedfilename <- "InstanceOrderedID.txt"
orderedFullFile <- paste(baseDir,orderedfilename,sep="\\")


#print("Pre-If Statement")
if(file.exists(orderedFullFile)){
  
  orderingfile <- read.table(orderedFullFile, header=F, sep=",", col.names=c("Instances"))
  print("Begin Correlation Calculations")
  
  correlationVector <- vector('double')
  
  for(i in orderingfile$Instances){
    
    
    theFile <- paste(i,"txt",sep=".")
    theFullFile <- paste(dir,theFile,sep="\\")
    if(file.exists(theFullFile)){
      file <- read.table(theFullFile, header=F, sep=",", col.names=c("TimeIndex","AvailableData"))
      
      filename <- basename(theFullFile)
      #print(filename)
      instanceType <- i
      print(instanceType)
      
      
      newsubfile <- paste(newDir,"CorrelationHeatMap",sep="\\")
      # print(newsubfile)
      
      
      corDataFile <- paste(newDir,filename,sep="\\")
      file.create(corDataFile)
      
      orderedfilename <- "InstanceOrderedID.txt"
      orderedFullFile <- paste(baseDir,orderedfilename,sep="\\")
      
      orderingfile <- read.table(orderedFullFile, header=F, sep=",", col.names=c("Instances"))
      print("Begin Correlation Calculations")
      line <- instanceType
      for(j in orderingfile$Instances){
        #print(j)
        compFile <- paste(j,"txt",sep=".")
        compFullFile <- paste(dir,compFile,sep="\\")
        #print(compFullFile)
        if(file.exists(compFullFile)){
          compFileTable <- read.table(compFullFile, header=F, sep=",", col.names=c("TimeIndex","AvailableData"))
          
          vec1 <- as.numeric(file$AvailableData)
          vec2 <- as.numeric(compFileTable$AvailableData)
          
          correlation <- cor(vec1, vec2)
          if(is.na(correlation)){
            correlation <- 0
          }
          correlationVector <- c(correlationVector,correlation)
          #line <- paste(line, correlation, sep=",")
          write(correlation,file=corDataFile,append=TRUE)
          #print(correlation)
        }
      }
      #print(line)
      #write(line,file=corDataFile,append=TRUE)
      
      
      
      
      
      
      #one file at a  time
      #break
    }
  }
  
 
  
  graphFile <- paste(newsubfile,"pdf",sep=".")
  
  ylimitlow <- -1
  ylimithigh <- 1
  
  pdf(graphFile)
 
  
  
  corMatrix <- matrix(correlationVector, nrow = 24, ncol = 24, #DIMENSIONS CHANGED TO 24 WITH REMOVAL OF T2
                      dimnames = list(orderingfile$Instances,orderingfile$Instances))
  
 
  
  my_palette <- colorRampPalette(c("white", "black"))(n = 256)
  #my_palette <- colorRampPalette(c("red", "yellow", "green"))(n = 299)
  
  heatmap.2(corMatrix,
            #cellnote = corMatrix,  # same data set for cell labels
           # main = "Correlation", # heat map title
            #notecol="black",      # change font color of cell labels to black
            density.info="none",  # turns off density plot inside color legend
            trace="none",         # turns off trace lines inside the heat map
            margins =c(12,9),     # widens margins around plot
            col=my_palette,       # use on color palette defined earlier
            #breaks=col_breaks,    # enable color transition at specified limits
            dendrogram="none",     # No Dendogram
            Colv="FALSE",
            Rowv = "FALSE") 
  
  dev.off()

}

print("Job Complete")
