package demo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventTypeToBinding {
	ORDER_EVENT("orderConsumer"),
	MENU_EVENT("menuConsumer");

	private final String binding;
}
