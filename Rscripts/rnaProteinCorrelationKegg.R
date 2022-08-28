
library("rjson")
library(dplyr)
library(ggrepel)


output = args[1]
comparison = args[2]

output = "/media/esteban/data/outputVariationPeptide2"
comparison = "Nsivssi"


allGenes = json_data <- fromJSON(file=paste(output, "allGenes.json", sep="/"))

dge = json_data <- fromJSON(file=paste(output, "dge", comparison, "dge.json", sep="/"))

kegg = read.csv("/home/esteban/Documents/PIT_reference_guided/kegg/genes.list_KEGG_hsa_functional_classification.tsv", sep="\t")


proteinPval = read.csv("/home/esteban/Documents/toolsTest/artsMS/output/results.txt", sep="\t")

keggFc = list()



for(gene in dge){

  if(!is.null(gene$padj) && gene$padj<0.05){
   
    for(keggPath in allGenes[[gene$symbol]][["kegg"]]){
      if(!is.null(keggPath)){

        if(!keggPath %in% names(keggFc)){
          keggFc[[keggPath]] = list(rna=c(), protein=c())
        }
        
      
        keggFc[[keggPath]][["rna"]] = c(keggFc[[keggPath]][["rna"]], gene$log2fc)

        if("ms"%in% names(gene)){
          runsCount=0
          proteinLog2Fc = 0

          if(nrow(proteinPval[proteinPval["Protein"]==gene$symbol,])>0 &&
             !is.na(proteinPval[proteinPval["Protein"]==gene$symbol, "pvalue"]) && proteinPval[proteinPval["Protein"]==gene$symbol, "pvalue"]<0.05){
            for(run in names(gene[["ms"]])){
              proteinLog2Fc = proteinLog2Fc + gene[["ms"]][[run]][["log2fc"]]
              runsCount = runsCount+1
            }
            keggFc[[keggPath]][["protein"]] = c(keggFc[[keggPath]][["protein"]], -proteinPval[proteinPval["Protein"]==gene$symbol, "log2FC"])
          }
          
        }
      }
      
    }
    
  }
}

rnaFc = c()
proteinFc = c()
geneRatios = c()
keggNames = c()

for(keggPath in names(keggFc)){

  if(!is.null(keggFc[[keggPath]][["protein"]])){
    res = dplyr::filter(kegg, grepl(keggPath, Process.name))
    name = strsplit(res$Process.name, "~")[[1]][2]
    geneRatio = length(keggFc[[keggPath]][["protein"]]) / res$num_of_Genes
    
    keggNames = c(keggNames, name)
    geneRatios = c(geneRatios, geneRatio)
    
    rnaMeanLog2Fc = mean(keggFc[[keggPath]][["rna"]])
    proteinMeanLog2Fc = mean(keggFc[[keggPath]][["protein"]])
    
    rnaFc = c(rnaFc, rnaMeanLog2Fc)
    proteinFc = c(proteinFc, proteinMeanLog2Fc)
  }
  
}

df = data.frame(rna=rnaFc, protein=proteinFc, geneRatios=geneRatios, pathway = keggNames)
#df = df[df["geneRatios"]>0.3,]




ggplot(df, aes(x=rna, y=protein, color=geneRatios)) + geom_point() + scale_color_gradient(low="blue", high="red") + geom_label_repel(aes(label = pathway),
                                                                                                                         box.padding   = 0.35,
                                                                                                                         point.padding = 0.5,
                                                                                                                         segment.color = 'grey50') 

