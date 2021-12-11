# pitsuite
A complete software suite for PIT analysis 






PITsuite is a software pipeline developped for the analysis and integration of RNA-Seq and Mass spectrometry data. 
![components](https://user-images.githubusercontent.com/19326121/143864818-4a23f6fe-7da1-46e8-8a6d-9243b52d3a3c.png)

PIT, the core analysis pipeline performs the analysis from the raw files. The results can then be imported into PITgui, a Java graphical user interface developped for this purpose.

Below are listed screenshots from PITgui, highlighting different features present in the application.


https://user-images.githubusercontent.com/19326121/145691226-767da92b-2ea6-46c1-ab25-e681d9dcea1c.mp4




![dge](https://user-images.githubusercontent.com/19326121/143865117-e0ca718d-9211-4c09-9b00-1b727cefb860.png)
1.Differential gene expression tab showing a table of differential gene expression and gene abundance for all genes identified with filters. A click on a row shows detailed information about a specific gene at the bottom. 2. Gene Set Enrichment analysis performed directly from PITgui at the RNA and protein level. PITgui supports enrichment of GO terms and KEGG pathways. 3. GO terms and KEGG pathways can also be used to filter the genes table or see the GO terms or KEGG pathway corresponding to the selected gene. A chosen KEGG pathway can be displayed and coloured according to the differential gene expression calculated by PIT.


![denovo](https://user-images.githubusercontent.com/19326121/143865412-4dc1fbb6-5ca3-418c-b1a8-9321c93eec54.png)
1. Tab in PITgui showing the different proteins identified in PIT and their matches from BLAST as well as the alignment and peptides showing evidence for the sequence at the protein level. 2. When clicking on a peptide, the corresponding annotated spectra can be visualised into PITgui. 3. For each peptide, a graph showing where this peptide maps as well as the protein ambiguity group can be displayed, helping determining uniqueness of the peptide. 4. Similar to the reference guided assembled transcripts, de novo transcripts and their coding sequences can be visualised in the gene browser. 5. Zooming in on the gene browser allows to see nucleotide and amino acid sequences.

![mutation](https://user-images.githubusercontent.com/19326121/143865560-67fb4f76-184e-4795-9f33-90de1abdab5a.png)
1. Table of mutations identified by PIT with filtering options. 2.Using filters, a SNP was selecting on the GSDMB gene, as it appears to be more prevalent in TCGA patients who are alive. 3. When selecting a mutation in the table, PITgui zooms in on this location and highlights the affected nucleotide. 4. Clicking on the mutation at the bottom displays the alternative protein sequence resulting from this mutation and if there are peptides mapping to this region.


![phospho](https://user-images.githubusercontent.com/19326121/143865699-06b01241-9d8b-49fc-b5da-165f68e024d5.png)
1. List of phosphosites identified with their fold change and pvalue between different conditions. The graph represents in blue the kinases known to phosphorylate this phosphote based on PhosphositePlus data. In addition, clicking on one of these kinase shows its over targets. 2. The right pane shows over information about the protein the phosphosite belongs to, such as differential gene expression or protein abundance or alternative splicing. 3. Kinase activty table as predicted by KSEA.

![splicing](https://user-images.githubusercontent.com/19326121/143865923-de2c3b82-6a1a-44c8-8b9f-40e3f1216913.png)
Discovery by PIT on a cryptic exon on STAG2. A. Representation of the splicing event in PITgui. Blue rectangles represent transcript exons, purple rectangle the coding sequence and pink rectangles peptides mapping to this position. B. PCR showing inclusion of the event. C. Psi (Percentage spliced in) in each condition based on PCR. D. Psi predicted by PIT based on RNA-Seq of si2. Relative abundance of this exon at the protein level based on peptide evidence.

