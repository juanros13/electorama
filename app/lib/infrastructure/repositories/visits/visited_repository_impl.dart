
import 'package:electorama_app/domain/datasources/visits/visited_datasource.dart';
import 'package:electorama_app/domain/repositories/visits/visited_repository.dart';

class VisitedRepositoryImpl extends VisitedRepository {
  
  final VisitedDataSource datasource;

  VisitedRepositoryImpl(this.datasource);
  
  @override
  Future<void> postVisited(Map<String, dynamic> visitedJson, int isarId) {
    return datasource.postVisited(visitedJson, isarId);
  }

}