# This shell script will run the following experiment: 
# =====================================================

# 1. Data set already splitted into individual days from 2015-01-01 to 2015-03-31
# 2. Now mine the maximal itemsets from each of the days with different support values

mkdir -p data/itemsets/maxm/1-00-00-00

for((i=0; i<=900; i+=5))
do
	x=$(echo "scale = 2; $i/100" | bc)
	mkdir -p data/itemsets/maxm/1-00-00-00/0"$x"

	for((f=1; f<=90; f++))
	do
		./fpgrowth data/timesplit/1-00-00-00/"$f".dat data/itemsets/maxm/1-00-00-00/0"$x"/"$f".dat -tm -s0"$x"
	done
done

# 3. Now detect the phenomenon from the maximal item sets by forming connected components

mkdir -p data/phenomenon/from_maxm/1-00-00-00

for((i=0; i<=900; i+=5))
do
	x=$(echo "scale = 2; $i/100" | bc)
	mkdir -p data/phenomenon/from_maxm/1-00-00-00/0"$x"

	for((f=1; f<=90; f++))
	do
		java BuildConnectedComponents maxm/1-00-00-00/0"$x"/"$f".dat from_maxm/1-00-00-00/0"$x"/"$f".dat
	done
done
