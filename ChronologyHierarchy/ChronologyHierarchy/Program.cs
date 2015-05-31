using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Globalization;
using System.Diagnostics;

namespace ChronologyHierarchy
{
    class Program
    {
        static void Main(string[] args)
        {
            var dateHash = @"C:\Users\Justin\Desktop\Persist\School\cs246\Phenomenon-Detection\OrganizePhenomenon\dataset\chronology\timestamp.collection";
            var biword = @"C:\Users\Justin\Desktop\Persist\School\cs246\Phenomenon-Detection\ChronologyHierarchy\ChronologyHierarchy\biword_assoc_0.15.dat";
            var monoword = @"C:\Users\Justin\Desktop\Persist\School\cs246\Phenomenon-Detection\ChronologyHierarchy\ChronologyHierarchy\monoword_assoc_0.15.dat";
            var phenomsFile = @"C:\Users\Justin\Desktop\Persist\School\cs246\Phenomenon-Detection\ChronologyHierarchy\ChronologyHierarchy\supp_0.15.txt";

            Dictionary<string, string> hashDate = new Dictionary<string, string>();
            Dictionary<string, int> biwordCounts = new Dictionary<string, int>();
            Dictionary<string, int> monowordCounts = new Dictionary<string, int>();
            Dictionary<string, int> setCounts = new Dictionary<string, int>();
            Dictionary<string, Dictionary<string, int>> hashSetCounts = new Dictionary<string, Dictionary<string, int>>();
            
            using (var reader = new StreamReader(dateHash))
            {
                var line = reader.ReadLine();
                
                while (line != null)
                {
                    var split = line.Split(' ');
                    var date = split[0] + " " + split[1];
                    var hash = split[2];
                    hashDate[hash] = date;
                    line = reader.ReadLine();
                }
            }

            using (var reader = new StreamReader(biword))
            {
                var line = reader.ReadLine();

                while (line != null)
                {
                    var spaceSplit = line.Split(' ');
                    var count = int.Parse(spaceSplit[1]);
                    var set = spaceSplit[0].Split('-');
                    var sets = string.Join(",", set);
                    biwordCounts[sets] = count;
                    foreach (var s in set)
                    {
                        if (!hashSetCounts.ContainsKey(s)){ hashSetCounts[s] = new Dictionary<string, int>(); }
                        hashSetCounts[s][sets] = count;
                    }
                    
                    line = reader.ReadLine();
                }
            }

            using (var reader = new StreamReader(monoword))
            {
                var line = reader.ReadLine();

                while (line != null)
                {
                    var spaceSplit = line.Split(' ');
                    var count = int.Parse(spaceSplit[1]);
                    var set = spaceSplit[0].Split('-');
                    var sets = string.Join(",", set);
                    monowordCounts[sets] = count;
                    foreach (var s in set)
                    {
                        if (!hashSetCounts.ContainsKey(s)) { hashSetCounts[s] = new Dictionary<string, int>(); }
                        hashSetCounts[s][sets] = count;
                    }

                    line = reader.ReadLine();
                }
            }

            foreach (var kp in monowordCounts)
            {
                setCounts[kp.Key] = kp.Value;
            }

            foreach (var kp in biwordCounts)
            {
                setCounts[kp.Key] = kp.Value;
            }

            List<List<Phenom>> phenomsList = new List<List<Phenom>>();

            using (var reader = new StreamReader(phenomsFile))
            {
                var line = reader.ReadLine();
                while (line != null)
                {
                    if (phenomsList.Count == 51)
                    {
                        Console.Write("");
                    }

                    var phenoms = Chronology(line, hashDate);
                    phenomsList.Add(phenoms);
                    line = reader.ReadLine();
                }
            }

            List<Tree> trees = new List<Tree>();

            foreach (var phenomList in phenomsList)
            {
                var clusterHash = new Dictionary<int, List<string>>();
                var hashCluster = new Dictionary<string, int>();
                foreach (var phenom in phenomList)
                {
                    var hash = phenom.Name;
                    var cluster = phenom.Cluster;
                    if (!clusterHash.ContainsKey(cluster)) { clusterHash[cluster] = new List<string>(); }
                    clusterHash[cluster].Add(hash);
                    hashCluster[hash] = cluster;
                }

                var compressed = Compress(setCounts, hashSetCounts, phenomList, clusterHash);

                var tree = new Tree(compressed, clusterHash, hashCluster);
                var ordered = phenomList.OrderBy(p => p.DateTime).Select(s=>s.Cluster).ToList();
                foreach (var node in ordered)
                {
                    tree.Add(node);
                }
                trees.Add(tree);
            }

            var treeOutput = "trees.dat";
            var lines = new List<string>();
            foreach (var tree in trees)
            {
                lines.Add(tree.ToString());
            }

            treeOutput.DeleteWrite(lines);

            Debug.WriteLine("<DONE>");
        }

        private static Dictionary<string, int> Compress(Dictionary<string, int> setCounts, Dictionary<string, Dictionary<string, int>> hashSetCounts, List<Phenom> phenomList, Dictionary<int, List<string>> clusterHash)
        {
            var compressed = new Dictionary<string, int>();
            foreach (var keyValue in setCounts)
            {
                compressed[keyValue.Key] = keyValue.Value;
            }

            var clusters = phenomList.Select(s => s.Cluster).Distinct().ToList();
            foreach (var cluster in clusters)
            {
                var ordered = clusterHash[cluster].OrderBy(o => o).ToList();
                var winner = ordered[0];
                for (var i = 1; i < ordered.Count; i++)
                {
                    var cur = ordered[i];
                    var curSetCounts = hashSetCounts[cur];
                    foreach (var keyVal in curSetCounts)
                    {
                        var set = keyVal.Key;
                        var setCount = keyVal.Value;
                        var toSetList = set.Split(',');
                        var replaced = toSetList.Select(s => s == cur ? winner : s).Distinct().OrderBy(o => o);
                        var replacedSet = string.Join(",", replaced);
                        if (compressed.ContainsKey(replacedSet))
                        {
                            compressed[replacedSet] += setCount;
                        }
                        else
                        {
                            compressed[replacedSet] = setCount;
                        }
                        compressed.Remove(set);
                    }
                }
            }
            return compressed;
        }

        public class TreeNode
        {
            public List<TreeNode> Children;
            public string Name;
            public int Level { get; set; }
            public int Frequency { get; set; }
            public override string ToString()
            {
		        return Name + ":" + Frequency.ToString() + ":" + Children.Count.ToString() + " ";
            }
        }

        public class Tree
        {
            Dictionary<string, TreeNode> _nodes = new Dictionary<string, TreeNode>();
            TreeNode _root = new TreeNode() { Children = new List<TreeNode>(), Name = "root", Level = 1, Frequency = 0 };
            private Dictionary<string, int> _setCounts;
            private Dictionary<int, List<string>> _clusterHash;
            private Dictionary<string, int> _hashCluster;
            private int _depth = 1;

            public Tree(Dictionary<string, int> setCounts, Dictionary<int, List<string>> clusterHash, Dictionary<string, int> hashCluster)
            {
                this._setCounts = setCounts;
                this._clusterHash = clusterHash;
                this._hashCluster = hashCluster;
            }

            public void Add(int node)
            {
                var ordered = _clusterHash[node].OrderBy(o => o).ToList();
                var winner = ordered[0];
                var maxScore = _setCounts.ContainsKey(winner) ? _setCounts[winner] : 0;
                var maxParent = _root;
                foreach (var kv in _nodes)
                {
                    var pair = string.Join(",", new[] { winner, kv.Key }.ToList().OrderBy(o => o));
                    var score = _setCounts.ContainsKey(pair) ? _setCounts[pair] : 0;
                    if (score > maxScore)
                    {
                        maxScore = score;
                        maxParent = kv.Value;
                    }
                }

                var treeNode = new TreeNode() { Children = new List<TreeNode>(), Name = winner, Level = maxParent.Level+1, Frequency = 0 };
                maxParent.Children.Add(treeNode);
                _depth = Math.Max(_depth, treeNode.Level);
                _nodes[winner] = treeNode;
            }

            public override string ToString()
            {
                return ToStringRec(_root, 0);
            }

            private string ToStringRec(TreeNode node, int depth)
            {
                var tree = new StringBuilder();
                for (int i = 0; i < depth; i++)
                {
                    tree.Append("\t");
                }
                tree.Append("-" + node.ToString() + "\n");
		        for(int i=0; i<node.Children.Count; i++)
		        {
                    var tmp = node.Children[i];
                    tree.Append(ToStringRec(tmp, depth + 1));
		        }
                return tree.ToString();
            }
        }

        public class Phenom
        {
            public string Name;
            public DateTime DateTime;
            public int Cluster = 0;
        }

        private static List<Phenom> Chronology(string line, Dictionary<string, string> hashDate)
        {
            var split = line.Split(' ');
            var items = split.ToList();
            List<Phenom> phenoms = new List<Phenom>();
            foreach (var item in items)
            {
                var name = item;
                var mapped = hashDate[name];
                var dateTime = DateTime.ParseExact(mapped, "yyyy-MM-dd HH:mm:ss", CultureInfo.InvariantCulture);
                var phenom = new Phenom() { Name = name, DateTime = dateTime };
                phenoms.Add(phenom);
            }

            phenoms = phenoms.OrderBy(o => o.DateTime).ToList();
            phenoms = MaxClustersDP(phenoms);

            return phenoms;
        }

        static readonly double ALPHA = 2;

        public class PhenomCount
        {
            public Phenom Phenom;
            public int Count;
        }

        private static List<Phenom> MaxClustersDP(List<Phenom> phenoms)
        {
            List<PhenomCount> phenomCounts = new List<PhenomCount>();
            phenoms.ForEach(f=>phenomCounts.Add(new PhenomCount() { Phenom = f, Count = 0}));
            for (int i = phenomCounts.Count - 1; i >= 0; i--)
            {
                var addedNextIndex = AddedNextIndex(phenomCounts, i);
                var addedCount = addedNextIndex == -1 ? 1 : 1 + phenomCounts[addedNextIndex].Count;
                var skippedCount = addedNextIndex == -1 ? 0 : phenomCounts[i + 1].Count;
                if (skippedCount > addedCount)
                {
                    phenomCounts[i].Count = phenomCounts[i + 1].Count;
                    phenomCounts[i].Phenom.Cluster = phenomCounts[i + 1].Phenom.Cluster;
                }
                else
                {
                    phenomCounts[i].Count = addedNextIndex == -1 ? 1 : phenomCounts[addedNextIndex].Count+1;
                    phenomCounts[i].Phenom.Cluster = addedNextIndex == -1 ? 1 : phenomCounts[addedNextIndex].Phenom.Cluster + 1;
                }
            }
            return phenoms;
        }

        private static int AddedNextIndex(List<PhenomCount> phenomCounts, int i)
        {
            var phenomI = phenomCounts[i].Phenom;
            for (var j = i; j < phenomCounts.Count; j++)
            {
                var phenomJ = phenomCounts[j].Phenom;
                if ((phenomJ.DateTime - phenomI.DateTime).TotalHours > 2 * ALPHA)
                {
                    return j;
                }
            }
            return -1;
        }

        private static List<Phenom> MaxClusters(List<Phenom> phenoms)
        {
            if (phenoms.Count == 0) { return phenoms; }

            var skipped = Deep(phenoms.Skip(1).ToList());
            
            var maxCommunity = phenoms.Max(m=>m.Cluster);
            var added = AddCommunity(phenoms);

            if (added.Count == skipped.Count)
            {
                MaxClusters(skipped);
                return SetCluster(Deep(phenoms), maxCommunity);
            }

            var skippedProcessed = MaxClusters(skipped);
            var skippedProcessedCount = skippedProcessed.Select(s => s.Cluster).Where(w => w > 0).Distinct().ToList().Count;

            var addedProcessed = MaxClusters(added);
            var addedProcessedCount = addedProcessed.Select(a => a.Cluster).Where(w => w > 0).Distinct().ToList().Count;

            var ret = Deep(phenoms);

            if (skippedProcessedCount > addedProcessedCount)
            {
                ret[0].Cluster = 0;
            }
            else
            {
                maxCommunity = addedProcessed.Count == 0 ? 0 : addedProcessed.Max(m => m.Cluster);
                SetCluster(ret, maxCommunity);
            }

            return ret;
        }

        private static List<Phenom> SetCluster(List<Phenom> ret, int maxCommunity)
        {
            var newCommunity = maxCommunity + 1;
            var first = ret[0];
            foreach (var p in ret)
            {
                if ((p.DateTime - first.DateTime).TotalHours <= ALPHA)
                {
                    p.Cluster = newCommunity;
                }
                else if ((p.DateTime - first.DateTime).TotalHours <= 2 * ALPHA)
                {
                    p.Cluster = -1;
                }
            }
            return ret;
        }

        private static List<Phenom> Deep(List<Phenom> phenoms)
        {
            var ret = new List<Phenom>();
            phenoms.ForEach(f => ret.Add(new Phenom() { Cluster = f.Cluster, DateTime = f.DateTime, Name = f.Name }));
            return ret;
        }

        private static List<Phenom> AddCommunity(List<Phenom> phenoms)
        {
            var deep = Deep(phenoms);
            var first = phenoms[0];
            var after = phenoms.Where(w => (w.DateTime - first.DateTime).TotalHours > 2 * ALPHA).OrderBy(o => o.DateTime).ToList();
            return after;
        }
    }
}
