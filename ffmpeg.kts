#!/usr/bin/env kscript

import java.io.File

fun clear(target: File) {
    target.delete()
    File("ffmpeg2pass-0.log").delete()
    File("ffmpeg2pass-0.log.mbtree.temp").delete()
}

fun ffmpeg(args: String) {
    Runtime.getRuntime()
        .exec("ffmpeg $args")
        .errorStream.copyTo(System.out)
}

if (args.isEmpty()) {
	println("ffmpeg.kts <file name> <from seconds> <duration seconds>")
} else {
	val src = File(args[0])
	val target = File("$src-result.${src.extension}")
	val start = args[1].toFloat()
	val time = args[2].toFloat() - start

	clear(target)
	if (src.extension == "mp3") {
		// ffmpeg("-y -ss $start -t $time -i $src -strict -2 -pass 1 -b:v 200k -f mp3 -movflags faststart -threads 0 /dev/null")
		// ffmpeg("-ss $start -t $time -i $src -strict -2 -pass 2 -b:v 200k -f mp3 -movflags faststart -threads 0 $target")
		val scale = 1.7
		ffmpeg("-y -ss $start -t $time -i $src -strict -2 -pass 1 -b:v 200k -f mp3 -movflags faststart -threads -filter:a atempo=$scale 0 /dev/null")
		ffmpeg("-ss $start -t $time -i $src -strict -2 -pass 2 -b:v 200k -f mp3 -movflags faststart -threads 0 -filter:a atempo=$scale $target")
	} else {
		// ffmpeg("-y -ss $start -t $time -i $src -strict -2 -pass 1 -b:v 200k -f mp4 -movflags faststart -threads 0 /dev/null")
		// ffmpeg("-ss $start -t $time -i $src -strict -2 -pass 2 -b:v 200k -f mp4 -movflags faststart -threads 0 $target")
		val scale = args.getOrNull(3)?.toDoubleOrNull() ?: 1.3
		ffmpeg("-y -ss $start -t $time -i $src -strict -2 -pass 1 -b:v 200k -f mp4 -movflags faststart -threads 0 -filter_complex [0:v]setpts=PTS/$scale[v];[0:a]atempo=$scale[a] -map [v] -map [a] /dev/null")
		ffmpeg("-ss $start -t $time -i $src -strict -2 -pass 2 -b:v 200k -f mp4 -movflags faststart -threads 0 -filter_complex [0:v]setpts=PTS/$scale[v];[0:a]atempo=$scale[a] -map [v] -map [a] $target")
	}
}
