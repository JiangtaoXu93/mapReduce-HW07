class SongInfo (track : String, du : String, ld : String, sh : String, ah : String, tp: String) extends Serializable{
  var TRACK_ID : String = track
  var DURATION : String = du
  var LOUDNESS : String = ld
  var SONG_HOTNESS : String = sh
  var ARTIST_HOT : String = ah
  var TEMPO : String = tp

//  private def tokens = line.split(";")
//  var TRACK_ID = tokens(0)
//  var ARTIST_ID = tokens(16)
//  var ALBUM = tokens(22)
//  var DURATION = tokens(5)
//  var LOUDNESS = tokens(6)
//  var SONG_HOTNESS = tokens(25)
//  var ARTIST_HOT = tokens(20)
//  var TEMPO = tokens(7)
//  var FAMILIARITY = tokens(19)
//  var KEY = tokens(8)
//  var KEY_CONFIDENCE = tokens(9)
//  var SONG_TITLE = tokens(24)
//  var ARTIST_ID_IN_TERM = tokens(0)
//  var ARTIST_TERM = tokens(1)


  def this(line : String) ={
    //    def tokens = line.split(";")
    this("","","","","","")
    def tokens = line.split(";")
    var TRACK_ID = tokens(0)
    this.TRACK_ID = tokens(16)
    this.DURATION = tokens(5)
    this LOUDNESS = tokens(6)
    this SONG_HOTNESS = tokens(25)
    this ARTIST_HOT = tokens(20)
    this TEMPO = tokens(7)
  }



  def this(value : Double, symbol: Symbol) ={
    //    def tokens = line.split(";")
    this("","","","","","")
    symbol match {
      case 'fuzzyLoudness => this.LOUDNESS = value.toString
    }
  }



  def calculateDistance(sf : SongInfo, symbol: Symbol): Double ={
    symbol match{
      case 'fuzzyLoudness => get1DimensionDistance(sf.LOUDNESS.toDouble, this.LOUDNESS.toDouble)
      case _ => 0.0
    }
  }

  def get1DimensionDistance(p1 : Double, p2 : Double) = math.abs(p1 - p2)
}

