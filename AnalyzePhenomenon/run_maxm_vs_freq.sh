# This shell script will run the following experiment:
# =====================================================

# Calculate the avg. no. of maximal itemsets/avg. no. of all frequent itemsets for different support values

for((i=0; i<=555; i+=5))
do
	s=$(echo "scale = 2; $i/100" | bc)
	lf=$(ls -l data/itemsets/freq/1-00-00-00/0"$s"/*.dat | wc -l | awk '{print $1}')
	lm=$(ls -l data/itemsets/maxm/1-00-00-00/0"$s"/*.dat | wc -l | awk '{print $1}')
	f=$(wc -l data/itemsets/freq/1-00-00-00/0"$s"/*.dat | tail -1 | awk '{print $1}')
	m=$(wc -l data/itemsets/maxm/1-00-00-00/0"$s"/*.dat | tail -1 | awk '{print $1}')
	r=$(echo "scale = 2; $m/$f" | bc)
	echo "$s,$lf,$lm,$f,$m,$r">> results/ratio_maxm_to_freq.csv
done

