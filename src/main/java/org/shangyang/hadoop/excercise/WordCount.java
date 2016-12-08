package org.shangyang.hadoop.excercise;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 
 * 可以直接运行，引用的是 source path 下面的 core-site.xml / hdfs-site.xml 中的配置文件
 * 
 * 单机模式的 WordCount，hadoop 会读取本地的单机模式的配置文件，src/main/resources/ 
 * 
 * 
 * @author 商洋
 *
 * @createTime：Dec 5, 2016 4:16:13 PM
 */
public class WordCount {

	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);
		
		private Text word = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			
			StringTokenizer itr = new StringTokenizer(value.toString());
			
			while (itr.hasMoreTokens()) {
				
				word.set(itr.nextToken());
				
				context.write(word, one);
				
			}
		}
	}

	public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context)	throws IOException, InterruptedException {
			
			int sum = 0;
			
			for (IntWritable val : values) {
				
				sum += val.get();
				
			}
			
			result.set(sum);
			
			context.write(key, result);
			
		}
	}

	public static void main(String[] args) throws Exception {
		
		String inputPath = new File(".").getCanonicalPath() + "/src/main/resources/input/wordcount";
		
		String outputPath = new File(".").getCanonicalPath() + "/src/main/resources/output"; // 记得要删除已有的 output folder
		
		FileUtil.fullyDelete( new File( outputPath ) ); // 霸道删除，可以删除本地文件目录
		
		Configuration conf = new Configuration();
		
		Job job = Job.getInstance(conf, "word count");
		
		job.setJarByClass(WordCount.class);
		
		job.setMapperClass(TokenizerMapper.class);
		
		job.setCombinerClass(IntSumReducer.class);
		
		job.setReducerClass(IntSumReducer.class);
		
		job.setOutputKeyClass(Text.class);
		
		job.setOutputValueClass(IntWritable.class);
		
		FileInputFormat.addInputPath(job, new Path( inputPath ));
		
		FileOutputFormat.setOutputPath(job, new Path( outputPath ));
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
