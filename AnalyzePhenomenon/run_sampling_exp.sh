# Sampling experiments

for z in 1day srs 1min 24mins size
do

mkdir -p data/itemsets/maxm/"$z"

for((i=0; i<=900; i+=5))
do
	x=$(echo "scale = 2; $i/100" | bc)
	mkdir -p data/itemsets/maxm/"$z"/0"$x"

	./fpgrowth data/timesplit/"$z"/1.dat data/itemsets/maxm/"$z"/0"$x"/1.dat -tm -s0"$x"
done

mkdir -p data/phenomenon/from_maxm/"$z"

for((i=0; i<=900; i+=5))
do
	x=$(echo "scale = 2; $i/100" | bc)
	mkdir -p data/phenomenon/from_maxm/"$z"/0"$x"

	java BuildConnectedComponents maxm/"$z"/0"$x"/1.dat from_maxm/"$z"/0"$x"/1.dat
done

done
