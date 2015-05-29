# Get Number of Phenomenon and their Lengths from different samples

for z in 1day srs 1min 24mins size 1-00-00-00
do

for((i=0; i<=900; i+=5))
do
	s=$(echo "scale = 2; $i/100" | bc)
	y=0
	if [ -e data/phenomenon/from_maxm/"$z"/0"$s"/1.dat ]
	then
		y=$(wc -l data/phenomenon/from_maxm/"$z"/0"$s"/1.dat | tail -1 | awk '{print $1}')
	fi
	echo "$s,$y">> results/sample_"$z"_num.csv
done

for((i=5; i<=900; i+=5))
do
	s=$(echo "scale = 2; $i/100" | bc)
	r="NA"
	if [ -e data/phenomenon/from_maxm/"$z"/0"$s"/1.dat ]
	then
		cat data/phenomenon/from_maxm/"$z"/0"$s"/1.dat | awk '{print NF}' >> tmp.len.0"$s"
		tlen=$(cat tmp.len.0"$s" | awk '{sum+=$1}; END {print sum}' | tail -1)
		tphen=$(wc -l tmp.len.0"$s" | tail -1 | awk '{print $1}')
		r=$(echo "scale = 2; ($tlen-$tphen)/$tphen" | bc)
		rm tmp.len.0"$s"
	fi
	echo "$s,$r" >> results/sample_"$z"_len.csv
done

done
