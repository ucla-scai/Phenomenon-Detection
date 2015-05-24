# This shell script will run the following experiment:
# =====================================================

# Calculate the number of phenomenon for different support values for the given day

for((i=0; i<=555; i+=5))
do
	s=$(echo "scale = 2; $i/100" | bc)
	y=$(wc -l data/phenomenon/1-00-00-00/0"$s"/"$1".dat | tail -1 | awk '{print $1}')
	echo "$s,$y">> results/num_of_phen_day_"$1".csv
done

