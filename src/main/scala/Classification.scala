import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Classification {
  def kCluster = 3
  def epsilon = 0.001

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("Million Classification")
    val sc = new SparkContext(conf)
    val input = sc.textFile("MillionSongSubset/song_info.csv")
    val songInfos = input.mapPartitionsWithIndex { (idx, iterate) => if (idx == 0) iterate.drop(1) else iterate }.map(line => new SongInfo(line)).persist()
    val centroids = new Array[SongInfo](kCluster)
    var i = 0
    for (e <- songInfos.take(3)){
      centroids(i) = e
      i = i + 1
    }

    val finalCentroids = kMeans(songInfos,centroids,'fuzzyLoudness )
    for (e <- 0 to 2){
      System.out.println(finalCentroids(e).LOUDNESS)
    }


  }


  def kMeans(songInfos : RDD[SongInfo],intitCentroids: Seq[SongInfo], symbol: Symbol): Seq[SongInfo] = {
    var centroids = intitCentroids
    for(i <- 0 to 9){
      // calculate cluster by input centroids
      var clusters = getClusterByCentroids(songInfos,centroids, symbol)
      var n = clusters.take(1)
      // recalculate centroids
      centroids = getCentroids(clusters, symbol)
    }

    return centroids
  }


  def getClusterByCentroids(songInfos :RDD[SongInfo],centroids: Seq[SongInfo],symbol: Symbol ) = {
    songInfos.groupBy(song => {
      centroids.reduceLeft((a, b) =>
        if ((song.calculateDistance(a, symbol) ) < (song.calculateDistance(b, symbol))) a
        else b).LOUDNESS})
  }

  def getCentroids(clusters : RDD[(String, Iterable[SongInfo])], symbol: Symbol ) : Seq[SongInfo]= {

    val centroids = clusters.map(key => {
      var sum = 0.0;
      var it = key._2
      for (i <- it){
        sum = i.LOUDNESS.toDouble + sum
      }
      new SongInfo(sum/it.size, symbol)
    }).collect().toList


    return centroids
  }




}

