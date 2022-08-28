library("pathview")

args = commandArgs(trailingOnly=TRUE)

df = read.csv("Rscripts/pathviewData.csv", row.names = 1)
# df=df[complete.cases(df), ]
# 
# 
# 
# d = data.frame(fc=df$log2FoldChange)
# rownames(d) = row.names(df)
# 


setwd("plots")
hsa04110 <- pathview(gene.data  = df,
                     pathway.id = args[3],
                     species    = "hsa",
                     gene.idtype = "SYMBOL",
                     limit      = list(gene=2, cpd=1))




