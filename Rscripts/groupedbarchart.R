require(reshape2)
require(ggplot2)
library(ggthemes)
library(ggpubr)

args = commandArgs(trailingOnly=TRUE)

data = read.csv(args[1], header=FALSE)
names(data) <- c('Group', 'Condition','Count')


p=ggbarplot(
  data, x = "Group", y = "Count", fill= "Group",
  add = c("mean_se", "jitter"),
  fill = "Condition",
  palette = c("#4fd882", "#2e9dd4"),
  ylab="Normalised read counts", position="dodge",
) + stat_compare_means(method="t.test", comparisons=list(unique(data["Condition"])))




pdf(args[2])
print(p)
dev.off()