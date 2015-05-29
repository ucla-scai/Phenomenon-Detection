# This shell script will run the following experiment: 
# =====================================================

# 1. Produce lifetime analysis results for all phenomenon across different support values
#
# Three results are of interest to us:
#	- (a) Histogram showing number of phenomenon vs life length
#	- (b) Histogram showing number of phenomenon adding new hashtags every day after day 0
#	- (c) Histogram showing length of new elements added every day after day 0

for((i=5; i<=90; i+=5))
do
	s=$(echo "scale = 2; $i/100" | bc)
	#java PhenomenonLifeAnalysis ../OrganizePhenomenon/dataset/final_phenomenon/supp_0"$s".txt data/phenomenon/from_maxm/1-00-00-00/0"$s" results/lifeanalysis/0"$s"
	cat results/lifeanalysis/0"$s"/phen_lifetime.dat | awk '{print $4}' | cut -d':' -f2 > results/lifeanalysis/0"$s"/phen_lifetime.histogram # (a)
	cat results/lifeanalysis/0"$s"/delta_phen.dat | awk '{print $2}' | sort | uniq -c > results/lifeanalysis/0"$s"/delta_phen_num.histogram # (b)
	while read line; 
	do 
		day=$(echo $line | awk '{print $2}'); 
		len=$(grep "$day " results/lifeanalysis/0"$s"/delta_phen.dat | awk '{print $3}' | cut -d':' -f2 | awk '{sum+=$1}; END {print sum}'); 
		n=$(grep "$day " results/lifeanalysis/0"$s"/delta_phen.dat | wc -l); 
		r=$(echo "scale = 2; $len/$n" | bc)
		echo $day $len $n $r; 
	done < results/lifeanalysis/0"$s"/delta_phen.dat > tmp
	sort -u tmp >  results/lifeanalysis/0"$s"/delta_phen_len.histogram 	# (c)
	rm tmp
done
