import 'package:electorama_app/domain/entities/visits/visited_entity.dart';
import 'package:electorama_app/infrastructure/models/visits/visited_model.dart';

class VisitedMapper {
  static VisitedEntity movieModelToEntity(VisitedModel visited) => VisitedEntity(
    municipalityId: visited.municipalityId,
    seccionId: visited.seccionId,
    coordinateZoneId: visited.coordinateZoneId,
    localityId: visited.localityId,
    visitDate: visited.visitDate,
    numberOfVisited: visited.numberOfVisited,
    userCreatedId: visited.userCreatedId,
    visitedPeople: visited.visitedPeople,
    referenceAuthId: visited.referenceAuthId
      // adult: moviedb.adult,
      // backdropPath: (moviedb.backdropPath != '') 
        // ? 'https://image.tmdb.org/t/p/w500${ moviedb.backdropPath }'
        // : 'https://sd.keepcalms.com/i-w600/keep-calm-poster-not-found.jpg',
      // genreIds: moviedb.genreIds.map((e) => e.toString()).toList(),
      // id: moviedb.id,
      // originalLanguage: moviedb.originalLanguage,
      // originalTitle: moviedb.originalTitle,
      // overview: moviedb.overview,
      // popularity: moviedb.popularity,
      // posterPath: (moviedb.posterPath != '')
        // ? 'https://image.tmdb.org/t/p/w500${ moviedb.posterPath }'
        // : 'https://www.movienewz.com/img/films/poster-holder.jpg',
      // releaseDate: moviedb.releaseDate != null ? moviedb.releaseDate! : DateTime.now(),
      // title: moviedb.title,
      // video: moviedb.video,
      // voteAverage: moviedb.voteAverage,
      // voteCount: moviedb.voteCount
    );

    // static Movie movieDetailsToEntity( MovieDetails moviedb ) => Movie(
    //   adult: moviedb.adult,
    //   backdropPath: (moviedb.backdropPath != '') 
    //     ? 'https://image.tmdb.org/t/p/w500${ moviedb.backdropPath }'
    //     : 'https://sd.keepcalms.com/i-w600/keep-calm-poster-not-found.jpg',
    //   genreIds: moviedb.genres.map((e) => e.name ).toList(),
    //   id: moviedb.id,
    //   originalLanguage: moviedb.originalLanguage,
    //   originalTitle: moviedb.originalTitle,
    //   overview: moviedb.overview,
    //   popularity: moviedb.popularity,
    //   posterPath: (moviedb.posterPath != '')
    //     ? 'https://image.tmdb.org/t/p/w500${ moviedb.posterPath }'
    //     : 'https://sd.keepcalms.com/i-w600/keep-calm-poster-not-found.jpg',
    //   releaseDate: moviedb.releaseDate,
    //   title: moviedb.title,
    //   video: moviedb.video,
    //   voteAverage: moviedb.voteAverage,
    //   voteCount: moviedb.voteCount
    // );

}
