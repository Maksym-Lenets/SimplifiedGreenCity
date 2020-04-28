package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.location.LocationDto;
import greencity.dto.place.PlaceByBoundsDto;
import greencity.entity.FavoritePlace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import static org.junit.Assert.assertEquals;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class FavoritePlaceWithLocationMapperTest {
    @InjectMocks
    private FavoritePlaceWithLocationMapper favoritePlaceWithLocationMapper;

    @Test
    public void convertTest() {
        FavoritePlace favoritePlace = ModelUtils.getFavoritePlace();
        LocationDto location = new LocationDto();
        location.setId(favoritePlace.getPlace().getLocation().getId());
        location.setLng(favoritePlace.getPlace().getLocation().getLng());
        location.setLat(favoritePlace.getPlace().getLocation().getLat());
        location.setAddress(favoritePlace.getPlace().getLocation().getAddress());

        PlaceByBoundsDto expected = new PlaceByBoundsDto(favoritePlace.getId(), favoritePlace.getName(), location);

        assertEquals(expected, favoritePlaceWithLocationMapper.convert(favoritePlace));
    }
}
