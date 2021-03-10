package nextstep.subway.line.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import nextstep.subway.common.exception.InvalidSectionException;
import nextstep.subway.station.domain.Station;

@Embeddable
public class Sections{

  private static final int MIN = 1;
  private static final int EMPTY = 0;

  @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
  private List<Section> sectionList = new ArrayList<>();

  public Sections(){ }

  private void validateSection(Station upStation, Station downStation){
    if(sectionList.size() < MIN) return;
    boolean isValidUpStation =  !sectionList.get(sectionList.size()-1)
        .getDownStation().equals(upStation);
    boolean isValidDownStation = sectionList.stream()
        .anyMatch(section -> section.getUpStation().equals(downStation) || section.getDownStation().equals(downStation));
    if(isValidUpStation) { throw new InvalidSectionException("상행역은 현재 노선의 하행 종점역이어야 합니다.");}
    if(isValidDownStation){ throw new InvalidSectionException("하행역은 노선에 이미 등록되어 있습니다."); }
  }

  public void add(Line line,Station upStation,Station downStation,int distance){
    validateSection(upStation,downStation);
    sectionList.add(new Section(line,upStation,downStation,distance));
  }

  public void remove(long stationId){
    if(getSize() == EMPTY) {throw new InvalidSectionException("삭제할 구간이 없습니다.");}
    if(getSize() == MIN) {throw new InvalidSectionException("구간이 1개남은경우 삭제할 수 없습니다.");}
    if(!isLastStation(stationId)) {throw new InvalidSectionException("노선의 종점이 아닌경우 삭제할 수 없습니다.");}
    sectionList.remove(getSize()-1);
  }

  public int getSize(){
    return this.sectionList.size();
  }

  public boolean isLastStation(long stationId){
    return sectionList.get(getSize() - 1)
        .getDownStation().getId()
        .equals(stationId);
  }

  public List<Section> getSections(){
    return this.sectionList;
  }

  public List<Station> getSortedStations(){
    return sectionList.stream().sorted()
        .flatMap(section -> Stream.of(section.getUpStation(),section.getDownStation()))
        .distinct()
        .collect(Collectors.toList());
  }
}
