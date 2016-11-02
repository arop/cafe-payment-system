CREATE OR REPLACE FUNCTION public.updateordertotals(IN new_order_value real, IN user_id uuid)
  RETURNS setof users AS
$BODY$
declare results users%rowtype;
BEGIN 
	RETURN QUERY
		UPDATE users 
		SET total_order_value = total_order_value + new_order_value
		WHERE id = user_id
		RETURNING *;

END$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.updateordertotals(real, uuid)
  OWNER TO ***REMOVED***;

 SELECT public.updateordertotals(4.0,'aa6b17df-dfd3-424a-8bf6-6cb484935929');