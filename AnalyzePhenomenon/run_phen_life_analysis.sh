# This shell script will run the following experiment: 
# =====================================================

# 1. Produce lifetime analysis results for all phenomenon across different support values

for((i=5; i<=90; i+=5))
do
	s=$(echo "scale = 2; $i/100" | bc)
	java PhenomenonLifeAnalysis consld/supp_0"$s".txt data/phenomenon/from_maxm/1-00-00-00/0"$s" results/lifeanalysis/0"$s"
done
