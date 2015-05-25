# This shell script will run the following experiment: 
# =====================================================

# 1. Calculate average length of phenomenon for different support values for any given day

f="$1"

for((i=0; i<=555; i+=5))
do
	s=$(echo "scale = 2; $i/100" | bc)
	cat data/phenomenon/from_maxm/1-00-00-00/0"$s"/"$f".dat | awk '{print NF}' >> tmp.len.0"$s"
	tlen=$(cat tmp.len.0"$s" | awk '{sum+=$1}; END {print sum}' | tail -1)
	tphen=$(wc -l tmp.len.0"$s" | tail -1 | awk '{print $1}')
	r=$(echo "scale = 2; ($tlen-$tphen)/$tphen" | bc)
	echo "$s,$tlen,$tphen,$r">> results/day"$f"_support_vs_LenOfPhenomenon.csv
	rm tmp.len.0"$s"
done

