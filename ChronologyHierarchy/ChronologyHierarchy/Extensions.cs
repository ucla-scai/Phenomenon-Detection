using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

namespace ChronologyHierarchy
{
    public static class Extensions
    {
        public static void DeleteWrite(this string file, List<string> lines)
        {
            file.Delete();
            File.WriteAllLines(file, lines);
        }
        
        public static void DeleteWrite(this string file, string lines)
        {
            file.Delete();
            File.WriteAllText(file, lines);
        }

        public static void Delete(this string file)
        {
            if (File.Exists(file)) { File.Delete(file); }
        }

        public static bool Is(this float f, int i)
        {
            return Math.Floor(f) == Math.Ceiling(f) && Convert.ToInt32(Math.Ceiling(f)) == i;
        }

        public static bool Is(this float f, float s)
        {
            return Math.Round(f, 4) == Math.Round(s, 4);
        }

        public static int ToInt(this float f)
        {
            return Convert.ToInt32(f);
        }

        public static decimal ToDecimal(this int i)
        {
            return Convert.ToDecimal(i);
        }

        public static float ToFloat(this int i)
        {
            return Convert.ToSingle(i);
        }

        public static float ToFloat(this decimal d)
        {
            return Convert.ToSingle(d);
        }
    }
}
