# shellcheck shell=sh
set -e
              
KAFKA_BOOTSTRAP="${SPRING_KAFKA_BOOTSTRAP_SERVERS:-bank-kafka:9092}"
              
while ! kafka-topics.sh --bootstrap-server "$KAFKA_BOOTSTRAP" --list > /dev/null 2>&1; do
	sleep 5
done
                            
create_topic() {
	TOPIC="$1"
	PARTITIONS="${2:-1}"
	REPLICATION="${3:-1}"
	CONFIG="${4:-}"

	if kafka-topics.sh --bootstrap-server "$KAFKA_BOOTSTRAP" --list | grep -Fxq "$TOPIC"; then
		echo "Topic $TOPIC already exists"
	else
		echo "Creating topic: $TOPIC"

	set -- kafka-topics.sh \
		--bootstrap-server "$KAFKA_BOOTSTRAP" \
		--create \
		--topic "$TOPIC" \
		--partitions "$PARTITIONS" \
		--replication-factor "$REPLICATION"

		if [ -n "$CONFIG" ]; then
			echo "Config: $CONFIG"
			OLD_IFS="$IFS"
			IFS=','
			for config_item in $CONFIG; do
				set -- "$@" --config "$config_item"
			done
			IFS="$OLD_IFS"
		fi
              
		# Выполняем
		"$@"
              
		echo "Topic $TOPIC created successfully"
	fi
}
