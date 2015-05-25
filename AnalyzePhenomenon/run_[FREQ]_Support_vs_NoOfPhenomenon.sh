# This shell script will run the following experiment: 
# =====================================================

rm -fR data/timesplit/*
rm -fR data/itemsets/*
rm -fR data/phenomenon/*

# 1. Split the entire input data into individual days from 2015-01-01 to 2015-03-31

java SplitByTime 2015/01/01-00:00:00 1-00-00-00

# 2. Now mine the frequent itemsets from each of the days with different support values

mkdir -p data/itemsets/1-00-00-00

for((i=0; i<=600; i+=5))
do
	echo "* Processed $i of Step 2"

	x=$(echo "scale = 2; $i/100" | bc)
	mkdir -p data/itemsets/1-00-00-00/0"$x"

	for((f=1; f<=90; f++))
	do
		./fpgrowth data/timesplit/1-00-00-00/"$f".dat data/itemsets/1-00-00-00/0"$x"/"$f".dat -s0"$x"
	done
done

# 3. Now detect the phenomenon from the frequent item sets by forming connected components
# CAUTION: BuildComponent can run for files containing less than 250K lines.
# 	   Therefore, running this experiment for first 45 days, as after that there are 2-3 files which has more lines.

mkdir -p data/phenomenon/1-00-00-00

for((i=0; i<=600; i+=5))
do
	echo "* Processed $i of Step 3"

	x=$(echo "scale = 2; $i/100" | bc)
	mkdir -p data/phenomenon/1-00-00-00/0"$x"

	for((f=1; f<=45; f++))
	do
		java BuildConnectedComponents 1-00-00-00/0"$x"/"$f".dat 1-00-00-00/0"$x"/"$f".dat
	done
done

# 4. Calculate average number of phenomenon over 45 days for different support values

for((i=0; i<=555; i+=5))
do
	s=$(echo "scale = 2; $i/100" | bc)
	y=$(wc -l data/phenomenon/1-00-00-00/0"$s"/*.dat | tail -1 | awk '{print $1}')
	r=$(echo "scale = 2; $y/45" | bc)
	echo "$s,$y,$r">> results/support_vs_NoOfPhenomenon.csv
done

