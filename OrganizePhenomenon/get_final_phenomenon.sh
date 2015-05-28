#Support to be provided as argument

mkdir -p dataset/final_phenomenon
s="$1"
cat ../AnalyzePhenomenon/data/phenomenon/final_phenomenon/list_"$s".dat | sed 's/\*//g' > tmp
while read line
do
	n=$(echo $line | awk '{print NF}')
	if [ $n -gt 1 ]
	then
		echo $line >> dataset/final_phenomenon/supp_"$s".txt
	fi
done < tmp
rm tmp
