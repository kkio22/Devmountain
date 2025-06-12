package nbc.devmountain.common.config;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmbeddingConverter implements AttributeConverter<float[], String> {
	@Override
	public String convertToDatabaseColumn(float[] vector) {
		if(vector == null){
			return null;
		}
		return IntStream.range(0, vector.length)
			.mapToObj(i -> Float.toString(vector[i]))
			.collect(Collectors.joining(","));
	}

	@Override
	public float[] convertToEntityAttribute(String dbData) {
		if(dbData == null || dbData.isBlank()){
			return new float[0];
		}
		String[] converter = dbData.split(",");
		float[] result = new float[converter.length];
		for(int i = 0; i < converter.length; i++){
			result[i] = Float.parseFloat(converter[i]); // float로 변환 과정
		}
		return result;
	}
}
