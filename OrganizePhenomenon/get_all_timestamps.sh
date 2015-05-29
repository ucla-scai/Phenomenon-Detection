# This shell script will run the following experiment: 
# =====================================================

# 1. Get the timestamp for all hashtags in the global consolidated file for the minimum possible support (here, 0.05)

cat ../AnalyzePhenomenon/data/input/tweets_hashtags.dat | sed 's/$/ /g' | sed 's/\#//g' | tr '[:upper:]' '[:lower:]' > tmp1.txt
cat dataset/final_phenomenon/supp_0.05.txt | sed 's/ /\n/g' > tmp2.txt

while read line
do
	t=$(grep " $line " -m1 tmp1.txt | awk '{print $1,$2}')
	echo $t $line >> timestamp.collection
done < tmp2.txt

rm tmp1.txt
rm tmp2.txt
