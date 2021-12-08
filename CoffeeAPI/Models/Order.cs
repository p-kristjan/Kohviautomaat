using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace CoffeeAPI.Models
{
    public class Order
    {
        public int Id { get; set; }
        public int UserId { get; set; }
        public string PaymentType { get; set; }
        public DateTime Date { get; set; }

        public ICollection<SubOrder> SubOrders { get; set; }
    }
}
