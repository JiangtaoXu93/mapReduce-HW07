import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import scala.util.{Random, Try}

/**
  * @author Ankita,Jiangtao
  */

object Classification {
  def kCluster = 3
  def epsilon = 0.001

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("Million Classification")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")
    val input = sc.textFile("input/song_info.csv")
    val songInfos = input.mapPartitionsWithIndex { (idx, iterate) => if (idx == 0) iterate.drop(1) else iterate }.map(line => new SongInfo(line))
    val centroids = new Array[SongInfo](kCluster)

    for (i <- 0 until kCluster){//generate random intial centroids
      centroids(i) = songInfos.takeSample(false,1)(0)
      if (!centroids(i).isValid('fuzzyLoudness)) centroids(i).LOUDNESS = (0.123 * i + 0.01).toString
      if (!centroids(i).isValid('fuzzyLength)) centroids(i).DURATION = (0.123 * i + 0.01).toString
      if (!centroids(i).isValid('fuzzyTempo)) centroids(i).TEMPO = (0.123 * i + 0.01).toString
      if (!centroids(i).isValid('fuzzyHotness)) centroids(i).SONG_HOTNESS = (0.123 * i + 0.01).toString
      if (!centroids(i).isValid('combinedHotness)){
        centroids(i).SONG_HOTNESS = (0.123 * i + 0.01).toString
        centroids(i).SONG_HOTNESS = (0.321 * i + 0.01).toString
      }
    }

    val fuzzyLoudnessCentroids = runKmeans(songInfos,centroids,'fuzzyLoudness )
    val fuzzyLoudnessClusters = getClusterByCentroids(songInfos,fuzzyLoudnessCentroids, 'fuzzyLoudness)
    val outString = StringBuilder.newBuilder
    outString.append(" ")
    val outData = fuzzyLoudnessClusters.map {x => x._2.map{y => (x._1 + " , " + y.getSymbol('fuzzyLoudness)+ "|")}}
    outData.saveAsTextFile("output/K-loudness")
    outString.clear()

    val fuzzyLengthCentroids = runKmeans(songInfos,centroids,'fuzzyLength )
    val fuzzyLengthClusters = getClusterByCentroids(songInfos,fuzzyLengthCentroids, 'fuzzyLength)
    val outDataDuration = fuzzyLengthClusters.map {x => x._2.map{y => (x._1 + " , " + y.getSymbol('fuzzyLength)+ "|")}}
    outDataDuration.saveAsTextFile("output/K-duration")
    outString.clear()

    val fuzzyTempoCentroids = runKmeans(songInfos,centroids,'fuzzyTempo )
    val fuzzyTempoClusters = getClusterByCentroids(songInfos,fuzzyTempoCentroids, 'fuzzyTempo)
    val outDataTempo = fuzzyTempoClusters.map {x => x._2.map{y => (x._1 + " , " + y.getSymbol('fuzzyTempo)+ "|")}}
    outDataTempo.saveAsTextFile("output/K-tempo")
    outString.clear()


//    for (e <- 0 until fuzzyHotness.size){
//      System.out.println(fuzzyHotness(e).getSymbol('fuzzyHotness))
//    }
//    System.out.println("")
//    for (e <- 0 until combinedHotnessCentroids.size){
//      System.out.println(combinedHotnessCentroids(e).getSymbol('combinedHotness))
//    }
  }

  def runKmeans(songInfos : RDD[SongInfo],centroids: Seq[SongInfo], symbol: Symbol): Seq[SongInfo] = {
    var filteredSI = songInfos.filter(si => si.isValid(symbol))
    kMeans(filteredSI,centroids,symbol)
  }


  def kMeans(songInfos : RDD[SongInfo],intitCentroids: Seq[SongInfo], symbol: Symbol): Seq[SongInfo] = {
    var centroids = intitCentroids
    for(i <- 0 to 9){
      // calculate cluster by input centroids
      var clusters = getClusterByCentroids(songInfos,centroids, symbol)
      // recalculate centroids
      centroids = getCentroids(clusters, symbol)
    }
    return centroids
  }


  def getClusterByCentroids(songInfos :RDD[SongInfo],centroids: Seq[SongInfo],symbol: Symbol ) = {
    songInfos.groupBy(song => {
      centroids.reduceLeft((a, b) =>
        if ((song.calculateDistance(a, symbol) ) < (song.calculateDistance(b, symbol))) a
        else b).getSymbol(symbol)})
  }


  def getCentroids(clusters : RDD[(String, Iterable[SongInfo])], symbol: Symbol ) : Seq[SongInfo]= {
    symbol match {
      case 'combinedHotness => get2DimensionCentroids(clusters, symbol)
      case _ => get1DimensionCentroids(clusters, symbol)
    }
  }

  def get1DimensionCentroids(clusters : RDD[(String, Iterable[SongInfo])], symbol: Symbol ) : Seq[SongInfo]= {
    val centroids = clusters.map(key => {
      var sum = 0.0
      var it = key._2
      for (i <- it){
        sum = i.getSymbol(symbol).toDouble + sum
      }
      new SongInfo(sum/it.size, symbol)
    }).collect().toList

    return centroids
  }

  def get2DimensionCentroids(clusters : RDD[(String, Iterable[SongInfo])], symbol: Symbol ) : Seq[SongInfo]= {
    val centroids = clusters.map(key => {
      var songSum = 0.0
      var artistSum = 0.0
      var it = key._2
      for (i <- it){
        songSum = i.SONG_HOTNESS.toDouble + songSum
        artistSum = i.ARTIST_HOT.toDouble + artistSum
      }
      new SongInfo(songSum/it.size, artistSum/it.size, symbol)
    }).collect().toList

    return centroids
  }





}

