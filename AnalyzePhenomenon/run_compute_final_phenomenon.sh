# This shell script will run the following experiment: 
# User support to be provided as argument
# =====================================================

# 1. Collect all daily phenomenon in a single file over the period of 90 days for a given support 

mkdir -p data/itemsets/all_daily_phenomenon
mkdir -p data/phenomenon/final_phenomenon

for((f=1; f<=90; f++))
do
	cat data/phenomenon/from_maxm/1-00-00-00/"$1"/"$f".dat >> data/itemsets/all_daily_phenomenon/list_"$1".dat
done

# 2. Now again form connected components from this daily phenomenon over 90 days to get the final phenomenon

java BuildConnectedComponents all_daily_phenomenon/list_"$1".dat final_phenomenon/list_"$1".dat

# 3. Produce the following results just for analysis, not to be used anywhere else.

cd data/itemsets/all_daily_phenomenon/
cat list_"$1".dat | sed 's/\*//g' | awk '{print NF}' > len_"$1".dat
paste len_"$1".dat list_"$1".dat | sort -n -r > analysis_"$1".dat 

cd ../../phenomenon/final_phenomenon/
cat list_"$1".dat | sed 's/\*//g' | awk '{print NF}' | sort | uniq -c > len_analysis_"$1".dat
